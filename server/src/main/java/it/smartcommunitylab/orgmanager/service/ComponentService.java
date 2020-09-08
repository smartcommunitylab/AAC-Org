package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchComponentException;
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.config.ModelsConfig.ComponentsConfiguration;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.ModelDTO;

@Service
public class ComponentService {

    private final static Logger logger = LoggerFactory.getLogger(ComponentService.class);

    @Autowired
    private ComponentsConfiguration componentsConfiguration;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    /*
     * Context
     */
    public String getOrgContext(String organization) {
        // components are listed in org sub-context
        return AACRoleDTO
                .concatContext(Constants.ROOT_ORGANIZATIONS, organization, Constants.ROOT_COMPONENTS);
    }

    public String getContext(String component) {
        // components are listed at root level
        return AACRoleDTO
                .concatContext(Constants.ROOT_COMPONENTS, component);
    }

    /*
     * Models TODO merge static model with /models/<component> definitions
     */
    public List<ModelDTO> listModels() {
        logger.debug("list models");
        List<ModelDTO> components = new ArrayList<ModelDTO>();

        // retrieve configuration
        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();

        // retrieves all properties used in the output
        for (Map<String, String> map : componentProperties) {
            String name = map.get(Constants.FIELD_NAME);
            String componentId = map.get(Constants.FIELD_COMPONENT_ID);
            String rolesString = map.get(Constants.FIELD_ROLES);
            List<String> roles = parseRoles(rolesString);
            // adds the component to the output list
            components.add(new ModelDTO(name, componentId, roles));
        }

        return components;
    }

    public ModelDTO getModel(String component) throws NoSuchComponentException {

        ModelDTO c = null;
        // retrieve configuration
        List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();

        // retrieves all properties used in the output
        for (Map<String, String> map : componentProperties) {
            String componentId = map.get(Constants.FIELD_COMPONENT_ID);
            if (componentId.equals(component)) {
                String name = map.get(Constants.FIELD_NAME);
                String rolesString = map.get(Constants.FIELD_ROLES);
                List<String> roles = parseRoles(rolesString);

                c = new ModelDTO(name, componentId, roles);
                break;
            }
        }

        if (c == null) {
            throw new NoSuchComponentException();
        }

        return c;

    }

    /*
     * Component handling
     */

    public List<String> listComponents()
            throws IdentityProviderAPIException {

        // components are listed in org sub-context
        String context = Constants.ROOT_COMPONENTS;
        return roleService.listSpaces(context).stream().map(r -> r.getSpace())
                .collect(Collectors.toList());
    }

    public List<String> listComponents(String organization)
            throws IdentityProviderAPIException {

        // components are listed in org sub-context
        String context = getOrgContext(organization);
        return roleService.listSpaces(context).stream().map(r -> r.getSpace())
                .collect(Collectors.toList());
    }

    public List<ComponentDTO> getComponents(String organization)
            throws IdentityProviderAPIException {

        List<String> spaces = listComponents(organization);

        List<ComponentDTO> components = new ArrayList<>();

        // fetch all role definitions and build component model
        for (String s : spaces) {

            try {
                components.add(getComponent(organization, s));
            } catch (NoSuchComponentException e) {
                // skip invalid component
            }

        }

        return components;

    }

    public ComponentDTO getComponent(String organization, String componentId)
            throws NoSuchComponentException, IdentityProviderAPIException {

        // components are listed in org sub-context
        String context = getOrgContext(organization);

        try {
            // we expect roles only for owner
            String owner = roleService.getSpaceOwner(context, componentId);
            // filter out system roles, keep only custom
            List<String> cRoles = roleService.getRoles(owner)
                    .stream()
                    .filter(r -> context.equals(r.getContext())
                            && componentId.equals(r.getSpace()))
                    .filter(r -> !Constants.ROLE_OWNER.equals(r.getRole())
                            && !Constants.ROLE_PROVIDER.equals(r.getRole()))
                    .map(r -> r.getRole())
                    .collect(Collectors.toList());

            return ComponentDTO.from(componentId, organization,owner, cRoles);

        } catch (NoSuchUserException e) {
            throw new NoSuchComponentException();
        }

    }

    public ComponentDTO addComponent(String organization, String componentId, String userId, List<String> roles)
            throws IdentityProviderAPIException, NoSuchUserException {

        // components are listed in org sub-context
        String context = getOrgContext(organization);

        // validate owner via idp
        BasicProfile profile = profileService.getUserProfileById(userId);

        logger.info("add component " + componentId + " org " + organization + " owner " + userId);

        // add space
        AACRoleDTO spaceRole = roleService.addSpace(context, componentId, profile.getUserId());

        // find all providers and enlist in space
        Set<String> providers = new HashSet<>(
                roleService.getSpaceProviders(Constants.ROOT_ORGANIZATIONS, organization));
        providers.add(profile.getUserId());
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, componentId);

        for (String provider : providers) {
            // add for each provider an entry
            roleService.addRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        if (roles == null || roles.isEmpty()) {
            roles = Collections.emptyList();

            // search for configuration for custom roles
            try {
                ModelDTO conf = getModel(componentId);
                roles = conf.getRoles();
            } catch (NoSuchComponentException e) {
                // conf is not required
            }
        }

        if (!roles.isEmpty()) {
            // we need to map roles definition with context
            List<String> rolesToAdd = roles.stream().map(r -> new AACRoleDTO(context, componentId, r).getAuthority())
                    .collect(Collectors.toList());

            // register for owner
            roleService.addRoles(profile.getUserId(), rolesToAdd);
        }

        return ComponentDTO.from(componentId, organization,profile.getUserId(), roles);

    }

    public void deleteComponent(String organization, String componentId)
            throws IdentityProviderAPIException, NoSuchSpaceException {

        logger.info("delete component " + componentId + " org " + organization);

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        try {
            // find owner
            String owner = roleService.getSpaceOwner(context, componentId);

            // find all providers and clear in space
            Set<String> providers = new HashSet<>(
                    roleService.getSpaceProviders(context, componentId));
            providers.add(owner);
            AACRoleDTO providerRole = AACRoleDTO.providerRole(context, componentId);

            for (String provider : providers) {
                // remove space role for each provider
                roleService.deleteRoles(provider, Collections.singletonList(providerRole.getAuthority()));
            }

            // remove all roles definition saved on owner, except owner
            List<String> cRoles = roleService.getRoles(owner)
                    .stream()
                    .filter(r -> context.equals(r.getContext())
                            && componentId.equals(r.getSpace())
                            && !Constants.ROLE_OWNER.equals(r.getRole()))
                    .map(r -> r.getAuthority())
                    .collect(Collectors.toList());

            roleService.deleteRoles(owner, cRoles);

            // remove space by deleting owner role
            roleService.deleteSpace(context, componentId, owner);

        } catch (NoSuchUserException e) {
            throw new NoSuchSpaceException();
        }

    }

    /*
     * Roles handling
     */
    public String addRole(String organization, String componentId, String role)
            throws IdentityProviderAPIException, NoSuchComponentException {

        // components are listed in org sub-context
        String context = getOrgContext(organization);

        // require component registration for organization
        ComponentDTO component = getComponent(organization, componentId);
        if (component == null) {
            throw new NoSuchComponentException();
        }

        try {
            // we save roles for owner
            String owner = roleService.getSpaceOwner(context, componentId);

            logger.info("add role " + role + " org " + organization + " component " + componentId);

            if (!component.getRoles().contains(role)) {

                // create role with context
                AACRoleDTO componentRole = new AACRoleDTO(context, componentId, role);
                String cRole = componentRole.getAuthority();

                // save for owner
                roleService.addRoles(owner, Collections.singletonList(cRole));

            }

            return role;
        } catch (NoSuchUserException e) {
            throw new NoSuchComponentException();
        }
    }

    public void deleteRole(String organization, String componentId, String role)
            throws IdentityProviderAPIException, NoSuchComponentException {

        // components are listed in org sub-context
        String context = getOrgContext(organization);

        // require component registration for organization
        ComponentDTO component = getComponent(organization, componentId);
        if (component == null) {
            throw new NoSuchComponentException();
        }

        try {
            // we save roles for owner
            String owner = roleService.getSpaceOwner(context, componentId);

            logger.info("delete role " + role + " org " + organization + " component " + componentId);

            if (component.getRoles().contains(role)) {

                // create role with context
                AACRoleDTO componentRole = new AACRoleDTO(context, componentId, role);
                String cRole = componentRole.getAuthority();

                // delete for owner
                roleService.deleteRoles(owner, Collections.singletonList(cRole));

            }

        } catch (NoSuchUserException e) {
            throw new NoSuchComponentException();
        }
    }

    /*
     * Spaces handling
     */

    public List<String> listComponentSpaces(String componentId)
            throws NoSuchComponentException, IdentityProviderAPIException {
        // spaces are listed in context
        String context = getContext(componentId);

        return roleService.listSpaces(context).stream().map(r -> r.getSpace())
                .collect(Collectors.toList());
    }

//    public List<String> listComponentSpace(String organization, String componentId)
//            throws NoSuchComponentException, IdentityProviderAPIException {
//        // Admin or org owner/provider can manage org spaces
//        if (!OrgManagerUtils.userHasAdminRights()
//                && !OrgManagerUtils.userIsOwner(organization)
//                && !OrgManagerUtils.userIsProvider(organization)) {
//            throw new AccessDeniedException("Access is denied: insufficient rights.");
//        }
//
//        // components are listed in context
//        String context = getContext(componentId);
//
//        // require component registration for organization
//        ComponentDTO component = getComponent(organization, componentId);
//        if (component == null) {
//            throw new NoSuchComponentException();
//        }
//
//        // we fetch all spaces and filter only those listed in org
//        List<String> componentSpaces = listComponentSpaces(componentId);
//        
//        //TODO move to space or to manager
//        List<String> orgSpaces = 
//        
//    }

    public String registerComponentSpace(String organization, String componentId, String space, String ownerId,
            List<String> providers)
            throws IdentityProviderAPIException, NoSuchComponentException {

        // components are listed in context
        String context = getContext(componentId);

        // require component registration for organization
        ComponentDTO component = getComponent(organization, componentId);
        if (component == null) {
            throw new NoSuchComponentException();
        }

        logger.info(
                "add component space " + space + " for " + componentId + " org " + organization + " owner " + ownerId);

        // add space
        String cSpace = getSpaceSlug(organization, space);
        AACRoleDTO spaceRole = roleService.addSpace(context, cSpace, ownerId);

        // ensure owner is a provider
        providers.add(ownerId);
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, cSpace);

        for (String provider : providers) {
            // add for each provider an entry
            roleService.addRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        // do not propagate roles to space. let manager handle
        return cSpace;

    }

    public void unregisterComponentSpace(String organization, String componentId, String space, boolean cleanup)
            throws IdentityProviderAPIException, NoSuchSpaceException {

        logger.info("delete component space " + space + " for " + componentId + " org " + organization);

        // components are listed in context
        String context = getContext(componentId);

        try {
            String cSpace = getSpaceSlug(organization, space);

            // find owner
            String owner = roleService.getSpaceOwner(context, cSpace);

            // find all providers and clear in space
            Set<String> providers = new HashSet<>(
                    roleService.getSpaceProviders(context, cSpace));
            providers.add(owner);
            AACRoleDTO providerRole = AACRoleDTO.providerRole(context, cSpace);

            for (String provider : providers) {
                // remove space role for each provider
                roleService.deleteRoles(provider, Collections.singletonList(providerRole.getAuthority()));
            }

            if (!cleanup) {
                // clear defined component roles for every space user
                // merge custom defined plus configuration
                Set<String> roles = new HashSet<>();

                try {
                    ModelDTO conf = getModel(componentId);
                    roles.addAll(conf.getRoles());
                } catch (NoSuchComponentException e) {
                    // conf is not required
                }

                // check component registration for organization
                try {
                    ComponentDTO component = getComponent(organization, componentId);
                    roles.addAll(component.getRoles());
                } catch (NoSuchComponentException e) {
                    // ignore
                }

                if (!roles.isEmpty()) {
                    // we need to map roles definition with context
                    List<String> rolesToDel = roles.stream().map(r -> new AACRoleDTO(context, cSpace, r).getAuthority())
                            .collect(Collectors.toList());
                    Collection<String> users = roleService.getSpaceUsers(context, cSpace);

                    for (String user : users) {
                        // we won't check if users have any role, since AAC lets us delete non-existent
                        logger.debug("remove component roles " + rolesToDel.toString() + " for user " + user);
                        roleService.deleteRoles(user, rolesToDel);
                    }
                }
            } else {
                // clear all component roles for every space user
                Collection<String> users = roleService.getSpaceUsers(context, cSpace);
                for (String user : users) {
                    List<String> rolesToDel = roleService
                            .getRoles(user).stream()
                            .filter(r -> (context.equals(r.getContext())
                                    && cSpace.equals(r.getSpace())
                                    && !Constants.ROLE_OWNER.equals(r.getRole())))
                            .map(r -> r.getAuthority())
                            .collect(Collectors.toList());

                    logger.debug("remove component roles " + rolesToDel.toString() + " for user " + user);
                    roleService.deleteRoles(user, rolesToDel);
                }

            }

            // remove space by deleting owner role
            roleService.deleteSpace(context, cSpace, owner);

        } catch (NoSuchUserException e) {
            throw new NoSuchSpaceException();
        }

    }

    private String getSpaceSlug(String organization, String space) {
        return organization + Constants.SLUG_SEPARATOR + space;
    }

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
