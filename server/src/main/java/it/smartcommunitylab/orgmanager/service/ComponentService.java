package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;

// TODO - org components may be multiple - for different subspaces
// TODO - update operation should take this into account updating everything at once
@Service
@Transactional(readOnly = true)
public class ComponentService {

    private final static Logger logger = LoggerFactory.getLogger(ComponentService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ComponentsConfiguration componentsConfiguration;

    @Autowired
    private RoleService roleService;

    /**
     * Lists all components.
     * 
     * @param pageable - Page number, size, etc.
     * @return - A page of components
     */
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
     * @throws IdentityProviderAPIException 
     */
    public List<ComponentConfigurationDTO> getConfigurations(long organizationId) throws NoSuchOrganizationException, IdentityProviderAPIException {

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
        List<ComponentConfigurationDTO> config = new LinkedList<ComponentConfigurationDTO>();

        for (Map<String, String> conf: componentsConfiguration.getComponents()) {
        	String componentId = conf.get(Constants.FIELD_COMPONENT_ID);
        	Set<User> componentOwners = roleService.getRoleUsers(AACRoleDTO.componentOrgOwner(componentId, organization.getSlug()).canonicalSpace(), Constants.ROLE_PROVIDER, false);
        	if (!componentOwners.isEmpty()) {
        		config.add(new ComponentConfigurationDTO(componentId));
        	}
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
            
            Set<String> old = new HashSet<>();
            
            
            for (Map<String, String> conf: componentsConfiguration.getComponents()) {
            	String componentId = conf.get(Constants.FIELD_COMPONENT_ID);
            	Set<User> componentOwners = roleService.getRoleUsers(AACRoleDTO.componentOrgOwner(componentId, organization.getSlug()).canonicalSpace(), Constants.ROLE_PROVIDER, true);
            	if (!componentOwners.isEmpty()) {
            		old.add(componentId);
            	}
            }

			// new roles that will need to be added
			Set<User> rolesToAdd = new HashSet<>();
			// roles to be removed, due to an old tenant not present in the
			// new configuration
			Set<User> rolesToRemove = new HashSet<>();
            
            for (ComponentConfigurationDTO conf: configurationDTOList) {
            	String componentId = conf.getComponentId();
            	// completely new, add it for the owners
            	if (!old.contains(componentId)) {
            		for (User owner: owners) {
            			User toAdd = new User();
            			toAdd.setUserId(owner.getUserId());
            			toAdd.setUsername(owner.getUsername());
            			if (toAdd.getRoles() == null) toAdd.setRoles(new HashSet<>());
            			toAdd.getRoles().add(AACRoleDTO.componentOrgOwner(componentId, organization.getSlug()));
            			rolesToAdd.add(toAdd);
            		}
            	}
            	old.remove(componentId);
            }
            
            if (!old.isEmpty()) {
            	Map<String, Set<Role>> removeMap = new HashMap<>();
            	for (String cId: old) {
            		String space = AACRoleDTO.componentOrgOwner(cId, organization.getSlug()).canonicalSpace();
            		String prefix = space + "/";
                	Set<User> componentUsers = roleService.getRoleUsers(space, null, true);
                	componentUsers.forEach(c -> {
                		if (!removeMap.containsKey(c.getUserId())) removeMap.put(c.getUserId(), new HashSet<>());
                		removeMap.get(c.getUserId()).addAll(
                				// all roles that are within component/org 
                				c.getRoles().stream().filter(r -> {
                					String canonical = r.canonicalSpace();
                					return canonical.equals(space) || canonical.startsWith(prefix);
                					
                				}).collect(Collectors.toSet()));
                	});
            	}
            	removeMap.entrySet().forEach(e -> {
        			User toDel = new User();
        			toDel.setUserId(e.getKey());
        			toDel.setRoles(e.getValue());
            		rolesToRemove.add(toDel);
            	});
            }

            // update in AAC
            roleService.addRoles(rolesToAdd);
            roleService.deleteRoles(rolesToRemove);

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
}
