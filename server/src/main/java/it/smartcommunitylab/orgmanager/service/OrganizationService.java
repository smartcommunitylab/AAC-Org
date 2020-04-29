package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;

@Service
public class OrganizationService {
    private final static Logger logger = LoggerFactory.getLogger(OrganizationService.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    public List<OrganizationDTO> listOrganizations()
            throws IdentityProviderAPIException {

        // orgs are listed in root context
        String context = Constants.ROOT_ORGANIZATIONS;
        Collection<AACRoleDTO> spaces = roleService.listSpaces(context);
        List<OrganizationDTO> orgs = new ArrayList<>();

        logger.debug("list orgs result " + orgs.toString());

        // parse and filter, will also apply permissions
        for (AACRoleDTO s : spaces) {
            try {
                orgs.add(getOrganization(s.getSpace()));
            } catch (AccessDeniedException | NoSuchOrganizationException e) {
                // skip
                logger.debug(e.getMessage());
            }
        }

        return orgs;
    }

    public OrganizationDTO getOrganization(String organization)
            throws NoSuchOrganizationException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // orgs are listed in root context
        String context = Constants.ROOT_ORGANIZATIONS;

        // TODO read from AAC when attributes are implemented
        AACRoleDTO orgRole = AACRoleDTO.ownerRole(context, organization);

        // find owner
        String ownerId = roleService.getSpaceOwner(context, organization);
        if (ownerId == null) {
            throw new NoSuchOrganizationException();
        }

        try {
            BasicProfile profile = profileService.getUserProfileById(ownerId);
            return OrganizationDTO.from(profile.getUsername(), orgRole);
        } catch (NoSuchUserException e) {
            throw new NoSuchOrganizationException();
        }

    }

    public OrganizationDTO addOrganization(String organization, String owner)
            throws IdentityProviderAPIException, InvalidArgumentException,
            NoSuchUserException {
        // Admin or context owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // orgs are listed in root context
        String context = Constants.ROOT_ORGANIZATIONS;

        // validate owner via idp
        BasicProfile profile = profileService.getUserProfile(owner);
        if (profile == null) {
            logger.error("owner " + owner + " does not exists");
            throw new InvalidArgumentException("The owner does not exists");
        }

        // add as space
        AACRoleDTO orgRole = roleService.addSpace(context, organization, owner);

        // also enlist owner as provider
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, organization);
        roleService.addRoles(owner, Collections.singletonList(providerRole.getAuthority()));

        return OrganizationDTO.from(owner, orgRole);
    }

    public void deleteOrganization(String organization)
            throws NoSuchOrganizationException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // orgs are listed in root context
        String context = Constants.ROOT_ORGANIZATIONS;

        // find owner
        String owner = roleService.getSpaceOwner(context, organization);

        // find all providers and clear
        Set<String> providers = new HashSet<>(
                roleService.getSpaceProviders(context, organization));
        providers.add(owner);
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, organization);

        for (String provider : providers) {
            // remove role for each provider
            roleService.deleteRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        // remove space
        roleService.deleteSpace(context, organization, owner);

    }

//    /**
//     * Lists the organizations in pages. The organizations may be filtered by name
//     * (case insensitive). If the user has the organization management scope, all
//     * organizations will be returned. Otherwise, only organizations the user is
//     * owner of will be returned.
//     * 
//     * @param name     - A substring of the wanted organizations, case insensitive
//     * @param pageable - Page number, size, etc.
//     * @return - Organizations the user is allowed to see
//     */
//    @Transactional(readOnly = true)
//    public Page<OrganizationDTO> listOrganizations(String name, Pageable pageable) {
//        if (name == null) {
//            // null is not accepted, but an empty string works just fine when a filter on
//            // the name is not desired
//            name = "";
//        }
//        logger.debug("list organizations matching name: " + name);
//
//        List<Organization> organizations = organizationRepository.findByNameIgnoreCaseContaining(name, pageable);
//
//        // users with admin rights can see all organizations
//        // users without admin rights can only see organizations they are part of
//        List<OrganizationDTO> organizationsListDTO = organizations.stream()
//                .filter(o -> (OrgManagerUtils.userHasAdminRights() || OrgManagerUtils.userIsMember(o.getSlug())))
//                .map(o -> OrganizationDTO.from(o))
//                .collect(Collectors.toList());
//
//        // returns filtered results as a page
//        return new PageImpl<OrganizationDTO>(organizationsListDTO, pageable, organizationsListDTO.size());
//    }
//
//    /**
//     * Creates an organization. The input organization is in a format fit for
//     * viewing, so it will be converted before storage.
//     * 
//     * @param organizationDTO - The organization, as an instance of the view type of
//     *                        Organization
//     * @return - The same organization, after being stored and then re-converted
//     *         into the view type
//     * @throws SystemException
//     * @throws InvalidArgumentException
//     */
//    public OrganizationDTO createOrganization(
//            String name, String slug, String description,
//            String owner, Contacts contacts,
//            String[] tags) throws SystemException, InvalidArgumentException {
//
//        // Checks if the user has the rights to perform this operation
//        if (!OrgManagerUtils.userHasAdminRights()) {
//            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
//        }
//
//        logger.debug("create organization with name " + name + " owner " + owner);
//
//        try {
//            Organization organization = new Organization();
//
//            // While id is the key, the organization's name must also be unique, regardless
//            // of case
//            if (organizationRepository.findByNameIgnoreCase(name) != null) {
//                // organization with the same normalized name, ignoring case, already exists
//                logger.error("organization with name " + name + " already exists");
//                throw new IllegalArgumentException("Organization " + name + " already exists.");
//            }
//
//            organization.setName(name);
//
//            // slug is unique
//            if (organizationRepository.findBySlug(slug) != null) {
//                // organization with the same slug already exists
//                logger.error("organization with slug " + slug + " already exists");
//                throw new InvalidArgumentException("An organization with the slug " + slug + " already exists.");
//            }
//
//            organization.setSlug(slug);
//
//            // update description, permit empty values
//            organization.setDescription(description);
//
//            // validate owner via idp
//            BasicProfile profile = profileService.getUserProfile(owner);
//            if (profile == null) {
//                logger.error("owner " + owner + " does not exists");
//                throw new InvalidArgumentException(
//                        "The owner does not exists: " + owner);
//            }
//            // contacts
//            if (contacts != null) {
//                organization.setContactsEmail(contacts.getEmail());
//                organization.setContactsName(contacts.getName());
//                organization.setContactsSurname(contacts.getSurname());
//                organization.setContactsWeb(contacts.getWeb());
//                organization.setContactsPhone(contacts.getPhone());
//                organization.setContactsLogo(contacts.getLogo());
//            }
//
//            // tags
//            if (tags != null) {
//                organization.setTags(tags);
//            }
//
//            // persists
//            organization = organizationRepository.save(organization);
//            logger.info("created new organization " + organization.toString());
//
//            // build the owner and give it the ROLE_PROVIDER role
//            // ID used by the identity provider for the owner
//            String userId = profile.getUserId();
//
//            User toAdd = new User();
//            toAdd.setUserId(userId);
//            toAdd.setUsername(owner);
//            toAdd.setRoles(Collections.singleton(AACRoleDTO.orgOwner(organization.getSlug())));
//            // save role in idp
//            roleService.addRoles(Collections.singleton(toAdd));
//
//            // convert into view format
//            return OrganizationDTO.from(organization);
//        } catch (IdentityProviderAPIException e) {
//            logger.error(e.getMessage());
//            throw new SystemException(e.getMessage(), e);
//        } catch (NoSuchUserException e) {
//            throw new InvalidArgumentException(
//                    "The owner does not exists: " + owner);
//        }
//    }

//    /**
//     * Enables the organization, identified by its ID.
//     * 
//     * @param id - ID of the organization to enable
//     * @return - The updated organization
//     * @throws NoSuchOrganizationException
//     */
//    public OrganizationDTO enableOrganization(long id) throws NoSuchOrganizationException {
//        if (!OrgManagerUtils.userHasAdminRights()) {
//            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
//        }
//
//        // finds the organization
//        Organization organization = organizationRepository.findById(id).orElse(null);
//        if (organization == null) {
//            throw new NoSuchOrganizationException();
//        }
//
//        // enable
//        logger.debug("enable organization " + String.valueOf(id));
//        organization.setActive(true);
//        // persists
//        organization = organizationRepository.save(organization);
//        logger.info("updated organization " + organization.toString());
//
//        return OrganizationDTO.from(organization);
//    }
//
//    /**
//     * Disables the organization, identified by its ID.
//     * 
//     * @param id - ID of the organization to disable
//     * @return - The updated organization
//     * @throws NoSuchOrganizationException
//     */
//    public OrganizationDTO disableOrganization(long id) throws NoSuchOrganizationException {
//        if (!OrgManagerUtils.userHasAdminRights()) {
//            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
//        }
//
//        // finds the organization
//        Organization organization = organizationRepository.findById(id).orElse(null);
//        if (organization == null) {
//            throw new NoSuchOrganizationException();
//        }
//        // disable
//        logger.debug("disable organization " + String.valueOf(id));
//        organization.setActive(false);
//        // persists
//        organization = organizationRepository.save(organization);
//        logger.info("updated organization " + organization.toString());
//
//        return OrganizationDTO.from(organization);
//    }
//
//    /**
//     * Deletes the specified organization.
//     * 
//     * @param id - ID of the organization
//     * @throws NoSuchOrganizationException
//     * @throws InvalidArgumentException,   SystemException
//     */
//    public void deleteOrganization(long id)
//            throws NoSuchOrganizationException, InvalidArgumentException, SystemException {
//        if (!OrgManagerUtils.userHasAdminRights()) {
//            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
//        }
//
//        // finds the organization
//        Organization organization = organizationRepository.findById(id).orElse(null);
//        if (organization == null) {
//            throw new NoSuchOrganizationException();
//        }
//
//        logger.debug("delete organization " + String.valueOf(id));
//
//        // active org can not be deleted
//        if (organization.isActive()) {
//            logger.error("organization is active");
//            throw new InvalidArgumentException(
//                    "Unable to delete organization with ID " + id + ": the organization must first be disabled.");
//        }
//
//        try {
//            // delete all roles within org spaces, resources, and components
//            Set<String> prefixes = roleService.getOrgPrefixes(organization.getSlug());
//            Set<User> rolesToRemove = new HashSet<>();
//            for (String pre : prefixes) {
//                Set<User> componentUsers = roleService.getRoleUsers(pre, null, true);
//                componentUsers.forEach(c -> {
//                    User toRemove = new User();
//                    toRemove.setUserId(c.getUserId());
//                    toRemove.setRoles(c.getRoles());
//                    rolesToRemove.add(toRemove);
//                });
//            }
//            roleService.deleteRoles(rolesToRemove);
//        } catch (IdentityProviderAPIException e) {
//            throw new SystemException(e.getMessage(), e);
//        }
//
//        // delete org roles
//        try {
//            Set<User> organizationMembers = roleService.getOrganizationMembers(organization.getSlug());
//            roleService.deleteRoles(organizationMembers);
//        } catch (IdentityProviderAPIException e) {
//            throw new SystemException(e.getMessage(), e);
//        }
//
//        // deletes the organization from db
//        organizationRepository.delete(organization);
//        logger.debug("successfully deleted organization " + organization.toString());
//    }

}
