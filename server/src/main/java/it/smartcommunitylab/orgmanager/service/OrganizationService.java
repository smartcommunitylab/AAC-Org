package it.smartcommunitylab.orgmanager.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentConfigurationDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO.Contacts;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;

@Service
@Transactional
public class OrganizationService {
    private final static Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrgManagerUtils utils;

    @Autowired
    private ComponentsModel componentsModel;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ComponentService componentService;

    /**
     * Lists the organizations in pages. The organizations may be filtered by name
     * (case insensitive). If the user has the organization management scope, all
     * organizations will be returned. Otherwise, only organizations the user is
     * owner of will be returned.
     * 
     * @param name     - A substring of the wanted organizations, case insensitive
     * @param pageable - Page number, size, etc.
     * @return - Organizations the user is allowed to see
     */
    @Transactional(readOnly = true)
    public Page<OrganizationDTO> listOrganizations(String name, Pageable pageable) {
        if (name == null) {
            // null is not accepted, but an empty string works just fine when a filter on
            // the name is not desired
            name = "";
        }
        logger.debug("list organizations matching name: " + name);
        List<Organization> organizations = organizationRepository.findByNameIgnoreCaseContaining(name, pageable);

        // users with admin rights can see all organizations
        // users without admin rights can only see organizations they are part of
        List<OrganizationDTO> organizationsListDTO = organizations.stream()
                .filter(o -> (utils.userHasAdminRights() || utils.userIsMember(o.getSlug())))
                .map(o -> OrganizationDTO.from(o))
                .collect(Collectors.toList());

        // returns filtered results as a page
        return new PageImpl<OrganizationDTO>(organizationsListDTO, pageable, organizationsListDTO.size());
    }

    /**
     * Creates an organization. The input organization is in a format fit for
     * viewing, so it will be converted before storage.
     * 
     * @param organizationDTO - The organization, as an instance of the view type of
     *                        Organization
     * @return - The same organization, after being stored and then re-converted
     *         into the view type
     * @throws SystemException
     * @throws InvalidArgumentException
     */
    public OrganizationDTO createOrganization(
            String name, String slug, String description,
            String owner, Contacts contacts,
            String[] tags) throws SystemException, InvalidArgumentException {

        // Checks if the user has the rights to perform this operation
        if (!utils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
        }

        logger.debug("create organization with name " + name + " owner " + owner);

        try {
            Organization organization = new Organization();

            // While id is the key, the organization's name must also be unique, regardless
            // of case
            if (organizationRepository.findByNameIgnoreCase(name) != null) {
                // organization with the same normalized name, ignoring case, already exists
                logger.error("organization with name " + name + " already exists");
                throw new IllegalArgumentException("Organization " + name + " already exists.");
            }

            organization.setName(name);

            // slug is unique
            if (organizationRepository.findBySlug(slug) != null) {
                // organization with the same slug already exists
                logger.error("organization with slug " + slug + " already exists");
                throw new InvalidArgumentException("An organization with the slug " + slug + " already exists.");
            }

            organization.setSlug(slug);

            // update description, permit empty values
            organization.setDescription(description);

            // validate owner via idp
            BasicProfile profile = profileService.getUserProfile(owner);
            if (profile == null) {
                logger.error("owner " + owner + " does not exists");
                throw new InvalidArgumentException(
                        "The owner does not exists: " + owner);
            }

            // do NOT validate match between contact and owner
            // they can be disjoint by design

            // contacts
            if (contacts != null) {
                organization.setContactsEmail(contacts.getEmail());
                organization.setContactsName(contacts.getName());
                organization.setContactsSurname(contacts.getSurname());
                organization.setContactsWeb(contacts.getWeb());
                organization.setContactsPhone(contacts.getPhone());
                organization.setContactsLogo(contacts.getLogo());
            }

            // tags
            if (tags != null) {
                organization.setTags(tags);
            }

            // persists
            organization = organizationRepository.save(organization);
            logger.info("created new organization " + organization.toString());

            // build the owner and give it the ROLE_PROVIDER role
            // ID used by the identity provider for the owner
            String userId = profile.getUserId();

            User toAdd = new User();
            toAdd.setUserId(userId);
            toAdd.setUsername(owner);
            toAdd.setRoles(Collections.singleton(AACRoleDTO.orgOwner(organization.getSlug())));
            // save role in idp
            roleService.addRoles(Collections.singleton(toAdd));

            UserInfo ownerUser = new UserInfo(owner, profile.getName(), profile.getSurname());

            // Performs the operation in the components
            Map<String, Component> componentMap = componentsModel.getListComponents();
            String resultMessage;
            for (String s : componentMap.keySet()) {
                resultMessage = componentMap.get(s).createUser(ownerUser);
                if (CommonUtils.isErroneousResult(resultMessage)) {
                    throw new EntityNotFoundException(resultMessage);
                }
                resultMessage = componentMap.get(s).createOrganization(name, ownerUser);
                if (CommonUtils.isErroneousResult(resultMessage)) {
                    throw new EntityNotFoundException(resultMessage);
                }
            }

            // convert into view format
            return OrganizationDTO.from(organization);
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        } catch (NoSuchUserException e) {
            throw new InvalidArgumentException(
                    "The owner does not exists: " + owner);
        }
    }

    /**
     * Updates an organization's info. Only description, contacts and tags may be
     * updated, any other field in the input will be ignored.
     * 
     * @param id              - The ID of the organization to alter
     * @param organizationDTO - The organization to alter, in view format
     * @return - The updated organization
     * @throws NoSuchOrganizationException
     */
    public OrganizationDTO updateOrganization(long id, String description, Contacts contacts, String[] tags)
            throws NoSuchOrganizationException {

        // finds the organization
        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        // Checks if the user has permission to perform this action
        if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug())) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.debug("update organization " + String.valueOf(id));

        // update description, permit empty values
        organization.setDescription(description);

        // contacts
        if (contacts != null) {
            organization.setContactsEmail(contacts.getEmail());
            organization.setContactsName(contacts.getName());
            organization.setContactsSurname(contacts.getSurname());
            organization.setContactsWeb(contacts.getWeb());
            organization.setContactsPhone(contacts.getPhone());
            organization.setContactsLogo(contacts.getLogo());
        }

        // tags
        if (tags != null) {
            organization.setTags(tags);
        }

        // persists
        organization = organizationRepository.save(organization);
        logger.info("updated organization " + organization.toString());

        // convert into view format
        return OrganizationDTO.from(organization);
    }

    /**
     * Enables the organization, identified by its ID.
     * 
     * @param id - ID of the organization to enable
     * @return - The updated organization
     * @throws NoSuchOrganizationException
     */
    public OrganizationDTO enableOrganization(long id) throws NoSuchOrganizationException {
        if (!utils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
        }

        // finds the organization
        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        // enable
        logger.debug("enable organization " + String.valueOf(id));
        organization.setActive(true);
        // persists
        organization = organizationRepository.save(organization);
        logger.info("updated organization " + organization.toString());

        return OrganizationDTO.from(organization);
    }

    /**
     * Disables the organization, identified by its ID.
     * 
     * @param id - ID of the organization to disable
     * @return - The updated organization
     * @throws NoSuchOrganizationException
     */
    public OrganizationDTO disableOrganization(long id) throws NoSuchOrganizationException {
        if (!utils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
        }

        // finds the organization
        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }
        // disable
        logger.debug("disable organization " + String.valueOf(id));
        organization.setActive(false);
        // persists
        organization = organizationRepository.save(organization);
        logger.info("updated organization " + organization.toString());

        return OrganizationDTO.from(organization);
    }

    /**
     * Deletes the specified organization.
     * 
     * @param id - ID of the organization
     * @throws NoSuchOrganizationException
     * @throws InvalidArgumentException,   SystemException
     */
    public void deleteOrganization(long id)
            throws NoSuchOrganizationException, InvalidArgumentException, SystemException {
        if (!utils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
        }

        // finds the organization
        Organization organization = organizationRepository.findById(id).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        logger.debug("delete organization " + String.valueOf(id));

        // active org can not be deleted
        if (organization.isActive()) {
            logger.error("organization is active");
            throw new InvalidArgumentException(
                    "Unable to delete organization with ID " + id + ": the organization must first be disabled.");
        }

        // update configuration in components
        List<ComponentConfigurationDTO> componentConfigs = componentService.getConfigurations(id);
        final Map<String, List<String>> tenantsToDelete = new HashMap<>();
        componentConfigs.forEach(c -> {
            tenantsToDelete.put(c.getComponentId(), new LinkedList<String>(c.getTenants()));
            c.setTenants(Collections.<String>emptySet());
        });
        componentService.updateConfigurations(id, componentConfigs);

        // delete roles
        try {
            Set<User> organizationMembers = roleService.getOrganizationMembers(organization.getSlug());
            roleService.deleteRoles(organizationMembers);
        } catch (IdentityProviderAPIException e) {
            throw new SystemException(e.getMessage(), e);
        }
        // Deletes the organization in the components
        Map<String, Component> componentMap = componentsModel.getListComponents();
        for (String s : componentMap.keySet()) {
            componentMap.get(s).deleteOrganization(organization.getName(), tenantsToDelete.get(s));
        }

        // deletes the organization from db
        organizationRepository.delete(organization);
        logger.debug("successfully deleted organization " + organization.toString());
    }
}
