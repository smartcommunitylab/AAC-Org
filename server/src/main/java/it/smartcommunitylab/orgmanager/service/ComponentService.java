package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.config.ComponentsConfig.ComponentsConfiguration;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentConfigurationDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;

@Service
public class ComponentService {

    private final static Logger logger = LoggerFactory.getLogger(ComponentService.class);

    @Autowired
    private ComponentsConfiguration componentsConfiguration;

    @Autowired
    private RoleService roleService;

    /*
     * Context
     */
    public String getOrgContext(String organization) {
        // components are listed in org sub-context
        return AACRoleDTO
                .concatContext(AACRoleDTO.ORGANIZATION_PREFIX + organization + AACRoleDTO.COMPONENTS_PATH);
    }

    public String getContext(String component) {
        // components are listed at root level
        return AACRoleDTO
                .concatContext(AACRoleDTO.COMPONENTS_PREFIX + component);
    }

    /*
     * Component handling
     */

    public List<String> listComponents(String organization)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // components are listed in org sub-context
        String context = getOrgContext(organization);
        return roleService.listSpaces(context).stream().map(r -> r.getSpace())
                .collect(Collectors.toList());
    }

    public List<ComponentDTO> getComponents(String organization)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // components are listed in org sub-context
        String context = getOrgContext(organization);
        List<String> spaces = listComponents(organization);

        List<ComponentDTO> components = new ArrayList<>();

        // fetch all role definitions and build component model
        for (String s : spaces) {
            // we expect roles only for owner
            String owner = roleService.getSpaceOwner(context, s);
            // keep only roles matching
            List<Role> cRoles = roleService.getRoles(owner)
                    .stream()
                    .filter(r -> context.equals(r.getContext()) && s.equals(r.getSpace()))
                    .collect(Collectors.toList());

            components.add(ComponentDTO.from(cRoles));
        }

        return components;

    }

    public List<ComponentDTO> listConfigurations() {
        logger.debug("list components");
        List<ComponentDTO> components = new ArrayList<ComponentDTO>();

        // retrieve configuration
        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();

        // retrieves all properties used in the output
        for (Map<String, String> map : componentProperties) {
            String name = map.get(Constants.FIELD_NAME);
            String componentId = map.get(Constants.FIELD_COMPONENT_ID);
            String rolesString = map.get(Constants.FIELD_ROLES);
            List<String> roles = parseRoles(rolesString);
            // adds the component to the output list
            components.add(new ComponentDTO(name, componentId, roles));
        }

        return components;
    }

    public ComponentDTO addComponent(String organization, String componentId, String owner)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // components are listed in org sub-context
        String context = getOrgContext(organization);

        // add space
        AACRoleDTO spaceRole = roleService.addSpace(context, componentId, owner);

        // find all providers and enlist in space
        Set<String> providers = new HashSet<>(
                roleService.getSpaceProviders(AACRoleDTO.ORGANIZATION_PREFIX, organization));
        providers.add(owner);
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, componentId);

        for (String provider : providers) {
            // add for each provider an entry
            roleService.addRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        // search for configuration for roles
        List<String> roles = Collections.emptyList();
        List<ComponentDTO> configurations = listConfigurations();
        for (ComponentDTO conf : configurations) {
            if (conf.getComponentId().equals(componentId)) {
                roles = conf.getRoles();
                break;
            }
        }

        if (!roles.isEmpty()) {
            // register for owner
            roleService.addRoles(owner, roles);
        }

        return ComponentDTO.from(componentId, roles);

    }

    /*
     * Spaces handling
     */

//    /**
//     * Lists all components.
//     * 
//     * @param pageable - Page number, size, etc.
//     * @return - A page of components
//     */
//    public Page<ComponentDTO> listComponents(Pageable pageable) {
//        logger.debug("list components");
//        List<ComponentDTO> componentListDTO = new ArrayList<ComponentDTO>();
//
//        // retrieve configuration
//        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();
//
//        // retrieves all properties used in the output
//        for (Map<String, String> map : componentProperties) {
//            String name = map.get(Constants.FIELD_NAME);
//            String componentId = map.get(Constants.FIELD_COMPONENT_ID);
//            String rolesString = map.get(Constants.FIELD_ROLES);
//            List<String> roles = parseRoles(rolesString);
//            // adds the component to the output list
//            componentListDTO.add(new ComponentDTO(name, componentId, roles));
//        }
//
//        // returns as a page
//        return new PageImpl<ComponentDTO>(componentListDTO, pageable, componentListDTO.size());
//    }
//
//    /**
//     * Returns the possible roles for the requested component
//     * 
//     * @param componentId - ID of the component
//     * @return - A list of possible roles for the component
//     */
//    public List<String> getComponentRoles(String componentId) {
//        logger.debug("get roles for component " + componentId);
//        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();
//        List<String> roles = new ArrayList<String>();
//
//        // retrieves all properties for each component
//        for (Map<String, String> map : componentProperties) {
//            if (componentId.equals(map.get(Constants.FIELD_COMPONENT_ID))) {
//                // it's the input component
//                roles = parseRoles(map.get(Constants.FIELD_ROLES));
//                break;
//            }
//        }
//
//        return roles;
//    }
//
//    /**
//     * Returns the organization's configuration.
//     * 
//     * @param organizationId - ID of the organization
//     * @return - The configuration of the organization
//     * @throws NoSuchOrganizationException
//     * @throws IdentityProviderAPIException
//     */
//    public List<ComponentConfigurationDTO> getConfigurations(String slug)
//            throws NoSuchOrganizationException, IdentityProviderAPIException {
//
//        // Checks if the user has permission to perform this action
//        if (!OrgManagerUtils.userHasAdminRights() && !OrgManagerUtils.userIsOwner(slug)) {
//            throw new AccessDeniedException(
//                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
//        }
//
//        logger.debug("get configuration for organization " + slug);
//
//        // Prepares the configuration, to show it as response. It will be a list with an
//        // element for each component.
//        List<ComponentConfigurationDTO> config = new LinkedList<ComponentConfigurationDTO>();
//
//        for (Map<String, String> conf : componentsConfiguration.getComponents()) {
//            String componentId = conf.get(Constants.FIELD_COMPONENT_ID);
//            String componentName = conf.get(Constants.FIELD_NAME);
//            Set<User> componentOwners = roleService.getRoleUsers(
//                    AACRoleDTO.componentOrgOwner(componentId, slug).canonicalSpace(), Constants.ROLE_PROVIDER, false);
//            if (!componentOwners.isEmpty()) {
//                config.add(new ComponentConfigurationDTO(componentId, componentName));
//            }
//        }
//        return config;
//    }
//
//    /**
//     * Updates the organization's configuration. Input expects a list of component
//     * configurations. Each element must provide the component's ID and an array of
//     * the tenants that will belong to such component. Tenants previously present,
//     * but missing in the new configuration, will be deleted. Tenants previously
//     * present and also present in the new configurations will not be altered. New
//     * tenants will be created and assigned to the component. If the configuration
//     * for a specific component is not specified, its tenants will not be altered.
//     * 
//     * @param organizationId       - Organization to configure
//     * @param configurationDTOList - Components whose tenants are to be updated
//     * @return - The full updated configuration of the organization
//     * @throws NoSuchOrganizationException
//     * @throws SystemException
//     * @throws InvalidArgumentException
//     */
//    public List<ComponentConfigurationDTO> updateConfigurations(String slug,
//            List<ComponentConfigurationDTO> configurationDTOList)
//            throws NoSuchOrganizationException, SystemException, InvalidArgumentException {
//        if (!OrgManagerUtils.userHasAdminRights()) {
//            throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
//        }
//
//        logger.debug("update configuration for organization " + slug);
//
//        try {
//            Set<User> owners = roleService.getOrganizationOwners(slug);
//
//            if (owners == null || owners.isEmpty()) {
//                throw new EntityNotFoundException("No owner of organization " + slug
//                        + " could be found, unable to update configuration.");
//            }
//
//            Set<String> old = new HashSet<>();
//
//            for (Map<String, String> conf : componentsConfiguration.getComponents()) {
//                String componentId = conf.get(Constants.FIELD_COMPONENT_ID);
//                Set<User> componentOwners = roleService.getRoleUsers(
//                        AACRoleDTO.componentOrgOwner(componentId, slug).canonicalSpace(),
//                        Constants.ROLE_PROVIDER, true);
//                if (!componentOwners.isEmpty()) {
//                    old.add(componentId);
//                }
//            }
//
//            // new roles that will need to be added
//            Set<User> rolesToAdd = new HashSet<>();
//            // roles to be removed, due to an old tenant not present in the
//            // new configuration
//            Set<User> rolesToRemove = new HashSet<>();
//
//            for (ComponentConfigurationDTO conf : configurationDTOList) {
//                String componentId = conf.getComponentId();
//                // completely new, add it for the owners
//                if (!old.contains(componentId)) {
//                    for (User owner : owners) {
//                        User toAdd = new User();
//                        toAdd.setUserId(owner.getUserId());
//                        toAdd.setUsername(owner.getUsername());
//                        if (toAdd.getRoles() == null)
//                            toAdd.setRoles(new HashSet<>());
//                        toAdd.getRoles().add(AACRoleDTO.componentOrgOwner(componentId, slug));
//                        rolesToAdd.add(toAdd);
//                    }
//                }
//                old.remove(componentId);
//            }
//
//            if (!old.isEmpty()) {
//                Map<String, Set<Role>> removeMap = new HashMap<>();
//                for (String cId : old) {
//                    String space = AACRoleDTO.componentOrgOwner(cId, slug).canonicalSpace();
//                    Set<User> componentUsers = roleService.getRoleUsers(space, null, true);
//                    componentUsers.forEach(c -> {
//                        if (!removeMap.containsKey(c.getUserId()))
//                            removeMap.put(c.getUserId(), new HashSet<>());
//                        removeMap.get(c.getUserId()).addAll(c.getRoles());
//                    });
//                }
//                removeMap.entrySet().forEach(e -> {
//                    User toDel = new User();
//                    toDel.setUserId(e.getKey());
//                    toDel.setRoles(e.getValue());
//                    rolesToRemove.add(toDel);
//                });
//            }
//
//            // update in AAC
//            roleService.addRoles(rolesToAdd);
//            roleService.deleteRoles(rolesToRemove);
//
//            return getConfigurations(slug);
//        } catch (IdentityProviderAPIException e) {
//            logger.error(e.getMessage());
//            throw new SystemException(e.getMessage(), e);
//        }
//    }
//
    protected List<String> parseRoles(String rolesString) {
        if (StringUtils.isEmpty(rolesString)) {
            return Collections.emptyList();
        }

        return StringUtils.commaDelimitedListToSet(rolesString).stream()
                .map(r -> r.trim())
                .filter(r -> !"".equals(r))
                .collect(Collectors.toList());
    }
}
