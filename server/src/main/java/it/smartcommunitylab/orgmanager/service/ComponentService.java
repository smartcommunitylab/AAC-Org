package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
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
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;
import it.smartcommunitylab.orgmanager.config.ComponentsConfig.ComponentsConfiguration;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentConfigurationDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.Tenant;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;
import it.smartcommunitylab.orgmanager.repository.TenantRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class ComponentService {

    private final static Logger logger = LoggerFactory.getLogger(ComponentService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ComponentsConfiguration componentsConfiguration;

    @Autowired
    private ComponentsModel componentsModel;

    @Autowired
    private RoleService roleService;

    /**
     * Lists all components.
     * 
     * @param pageable - Page number, size, etc.
     * @return - A page of components
     */
    @Transactional(readOnly = true)
    public Page<ComponentDTO> listComponents(Pageable pageable) {
        logger.debug("list components");
        List<ComponentDTO> componentListDTO = new ArrayList<ComponentDTO>();

        // retrieve configuration
        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();

        // retrieves all properties used in the output
        for (Map<String, String> map : componentProperties) {
            String name = map.get(Constants.FIELD_NAME);
            String componentId = map.get(Constants.FIELD_COMPONENT_ID);
            String scope = map.get(Constants.FIELD_SCOPE);
            String format = map.get(Constants.FIELD_FORMAT);
            String implementation = map.get(Constants.FIELD_IMPLEMENTATION);
            String rolesString = map.get(Constants.FIELD_ROLES);
            List<String> roles = parseRoles(rolesString);
            // adds the component to the output list
            componentListDTO.add(new ComponentDTO(name, componentId, scope, format, implementation, roles));
        }

        // returns as a page
        return new PageImpl<ComponentDTO>(componentListDTO, pageable, componentListDTO.size());
    }

    /**
     * Returns the possible roles for the requested component
     * 
     * @param componentId - ID of the component
     * @return - A list of possible roles for the component
     */
    public List<String> getComponentRoles(String componentId) {
        logger.debug("get roles for component " + componentId);
        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();
        List<String> roles = new ArrayList<String>();

        // retrieves all properties for each component
        for (Map<String, String> map : componentProperties) {
            if (componentId.equals(map.get(Constants.FIELD_COMPONENT_ID))) {
                // it's the input component
                roles = parseRoles(map.get(Constants.FIELD_ROLES));
                break;
            }
        }

        return roles;
    }

    /**
     * Returns the organization's configuration.
     * 
     * @param organizationId - ID of the organization
     * @return - The configuration of the organization
     * @throws NoSuchOrganizationException
     */
    public List<ComponentConfigurationDTO> getConfigurations(long organizationId) throws NoSuchOrganizationException {

        // find the organization
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        // Checks if the user has permission to perform this action
        if (!OrgManagerUtils.userHasAdminRights() && !OrgManagerUtils.userIsOwner(organization.getSlug())) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.debug("get configuration for organization " + String.valueOf(organizationId));

        // Prepares the configuration, to show it as response. It will be a list with an
        // element for each component.
        List<ComponentConfigurationDTO> config = new ArrayList<ComponentConfigurationDTO>();
        Map<String, ComponentConfigurationDTO> configMap = new HashMap<String, ComponentConfigurationDTO>();
        // fetch the organization's tenants
        List<Tenant> tenants = tenantRepository.findByOrganization(organization);

        for (Tenant t : tenants) {
            String componentId = t.getTenantId().getComponentId();
            ComponentConfigurationDTO conf = configMap.get(componentId);
            if (conf == null) {
                conf = new ComponentConfigurationDTO(componentId, null);
                configMap.put(componentId, conf);
            }
            conf.addTenant(t.getTenantId().getName());
        }

        for (String s : configMap.keySet()) {
            config.add(configMap.get(s));
        }

        return config;
    }

    /**
     * Updates the organization's configuration. Input expects a list of component
     * configurations. Each element must provide the component's ID and an array of
     * the tenants that will belong to such component. Tenants previously present,
     * but missing in the new configuration, will be deleted. Tenants previously
     * present and also present in the new configurations will not be altered. New
     * tenants will be created and assigned to the component. If the configuration
     * for a specific component is not specified, its tenants will not be altered.
     * 
     * @param organizationId       - Organization to configure
     * @param configurationDTOList - Components whose tenants are to be updated
     * @return - The full updated configuration of the organization
     * @throws NoSuchOrganizationException
     * @throws SystemException
     * @throws InvalidArgumentException
     */
    public List<ComponentConfigurationDTO> updateConfigurations(long organizationId,
            List<ComponentConfigurationDTO> configurationDTOList)
            throws NoSuchOrganizationException, SystemException, InvalidArgumentException {
        if (!OrgManagerUtils.userHasAdminRights()) {
            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
        }

        // find the organization
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        logger.debug("update configuration for organization " + String.valueOf(organizationId));

        // TODO rework logic
        // use only AAC? why keep local if info is fetched from AAC
        try {
            Set<User> owners = roleService.getOrganizationOwners(organization.getSlug());

            if (owners == null || owners.isEmpty()) {
                throw new EntityNotFoundException("No owner of organization " + organization.getSlug()
                        + " could be found, unable to update configuration.");
            }

            // previous configuration
            List<Tenant> previousTenants = tenantRepository.findByOrganization(organization);
            // new configuration
            List<Tenant> newTenants = validateConfiguration(organization, configurationDTOList);

            // new roles that will need to be added
            Set<User> rolesToAdd = new HashSet<>();
            // roles to be removed, due to an old tenant not present in the
            // new configuration
            Set<User> rolesToRemove = new HashSet<>();

            // loops on all tenants listed in the new configuration
            for (Tenant t : newTenants) {
                // tenant is still in use
                previousTenants.remove(t);
                for (User owner : owners) {
                    owner.getRoles()
                            .add(AACRoleDTO.tenantOwner(t.getTenantId().getComponentId(), t.getTenantId().getName()));
                    owner.getRoles()
                            .add(AACRoleDTO.orgMember(organization.getSlug()));
                    rolesToAdd.add(owner);
                }
            }

            Set<String> componentIds = configurationDTOList.stream()
                    .map(ComponentConfigurationDTO::getComponentId)
                    .collect(Collectors.toSet());

            // tenants no longer in use, to be removed
            List<Tenant> tenantsToRemove = new ArrayList<Tenant>();
            for (Tenant t : previousTenants) {
                // Loops on tenants not present in the new configuration. If a tenant does not
                // appear
                // because its component ID is not specified, however, it is interpreted as
                // "leave this component's configuration as it is".
                if (componentIds.contains(t.getTenantId().getComponentId())) {
                    // component ID was specified, but the
                    // tenant is missing
                    Role role = AACRoleDTO.tenantUser(t.getTenantId().getComponentId(), t.getTenantId().getName());
                    Set<User> toRemove = roleService.getRoleUsers(role.canonicalSpace());
                    rolesToRemove.addAll(toRemove);
                    tenantsToRemove.add(t);
                }
            }

            // update in db
            tenantRepository.saveAll(newTenants); // saves new tenants
            tenantRepository.deleteAll(tenantsToRemove); // deletes unused tenants

            // update in AAC
            roleService.addRoles(rolesToAdd);
            roleService.deleteRoles(rolesToRemove);

            // update in components
            Map<String, Component> componentMap = componentsModel.getListComponents();
            for (Tenant t : newTenants) {
                // Creates new tenants
                Component component = componentMap.get(t.getTenantId().getComponentId());
                if (component != null) {
                    Organization org = t.getOrganization();
                    UserInfo userInfo = new UserInfo(
                            org.getContactsEmail(),
                            org.getContactsName(),
                            org.getContactsSurname());

                    String resultMessage = component.createTenant(t.getTenantId().getName(),
                            t.getOrganization().getName(),
                            userInfo);
                    if (CommonUtils.isErroneousResult(resultMessage)) {
                        throw new EntityNotFoundException(resultMessage);
                    }
                }
            }
            for (Tenant t : tenantsToRemove) {
                // Deletes tenants no longer in use
                Component component = componentMap.get(t.getTenantId().getComponentId());
                String resultMessage = component.deleteTenant(t.getTenantId().getName(), t.getOrganization().getName());
                if (CommonUtils.isErroneousResult(resultMessage)) {
                    throw new EntityNotFoundException(resultMessage);
                }
            }

            return getConfigurations(organizationId);
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }

    protected List<String> parseRoles(String rolesString) {
        if (StringUtils.isEmpty(rolesString)) {
            return Collections.emptyList();
        }

        return StringUtils.commaDelimitedListToSet(rolesString).stream()
                .map(r -> r.trim())
                .filter(r -> !"".equals(r))
                .collect(Collectors.toList());
    }

    /**
     * Validates the input configuration. Also returns the full list of tenants that
     * appear in this new configuration.
     * 
     * @param organization         - Organization to configure
     * @param configurationDTOList - Components whose tenants are to be validated
     * @return - Full list of all tenants appearing in the new configuration
     * @throws InvalidArgumentException
     */
    private List<Tenant> validateConfiguration(
            Organization organization,
            List<ComponentConfigurationDTO> configurationDTOList) throws InvalidArgumentException {

        if (configurationDTOList == null || configurationDTOList.isEmpty()) {
            throw new InvalidArgumentException("No configuration specified.");
        }

        List<Tenant> newTenants = new ArrayList<Tenant>();

        // loop on the input configurations
        for (ComponentConfigurationDTO conf : configurationDTOList) {
            if (conf == null || conf.getComponentId() == null) {
                throw new InvalidArgumentException("The componentId field was not specified.");
            }

            String componentId = conf.getComponentId();
            boolean componentIdFound = false;
            String componentIdProperty;
            String componentTenantPattern = "";
            for (Map<String, String> map : componentsConfiguration.getComponents()) {
                // checks that the component ID
                // belongs to an actual component
                componentIdProperty = map.get(Constants.FIELD_COMPONENT_ID);
                if (componentIdProperty != null && componentIdProperty.equals(componentId)) {
                    // component ID is valid
                    componentIdFound = true;
                    componentTenantPattern = map.get(Constants.FIELD_FORMAT);
                    break;
                }
            }
            if (!componentIdFound) {
                // no component with the given ID could be found
                throw new InvalidArgumentException("Component " + componentId + " could not be found.");
            }

            // tenants need to have a certain format
            Pattern pattern = Pattern.compile(componentTenantPattern);
            if (conf.getTenants() != null) {
                for (String t : conf.getTenants()) {
                    if (!pattern.matcher(t).matches()) {
                        // tenant does not match the format
                        throw new InvalidArgumentException("The following tenant contains illegal characters: " + t
                                + ", please match this regex: " + componentTenantPattern);
                    }

                    // check if tenant already exists
                    Tenant storedTenant = tenantRepository.findByComponentIdAndName(componentId, t);
                    if (storedTenant == null) {
                        // tenant needs to be created
                        storedTenant = new Tenant(componentId, t, organization);
                    } else if (!storedTenant.getOrganization().getId().equals(organization.getId())) {
                        // cannot add this tenant
                        throw new InvalidArgumentException(
                                "Tenant " + storedTenant + " is already in use by a different organization.");
                    }

                    // adds the tenant to the list of tenants of the new configuration
                    newTenants.add(storedTenant);
                }
            } else {
                // component ID was specified, but the tenant field is missing
                throw new InvalidArgumentException("Component " + conf.getComponentId()
                        + " was found, but the tenants field was"
                        + " absent or incomplete. If you meant to delete all tenants for this component, please specify the tenant"
                        + " field as an empty array. If you don't want to change the configuration of this component, remove its"
                        + " configuration entirely.");
            }
        }

        return newTenants;
    }
}
