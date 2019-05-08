package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.config.ComponentsConfig;
import it.smartcommunitylab.orgmanager.config.SecurityConfig;
import it.smartcommunitylab.orgmanager.config.ComponentsConfig.ComponentsConfiguration;
import it.smartcommunitylab.orgmanager.dto.ComponentConfigurationDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;
import it.smartcommunitylab.orgmanager.model.Role;
import it.smartcommunitylab.orgmanager.model.Tenant;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;
import it.smartcommunitylab.orgmanager.repository.RoleRepository;
import it.smartcommunitylab.orgmanager.repository.TenantRepository;

@Service
@Transactional
public class ComponentService {
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private TenantRepository tenantRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private ComponentsConfiguration componentsConfiguration;
	
	@Autowired
	private ComponentsModel componentsModel;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private OrgManagerUtils utils;
	
	@Autowired
	private SecurityConfig securityConfig;
	
	/**
	 * Lists all components.
	 * 
	 * @param pageable - Page number, size, etc.
	 * @return - A page of components
	 */
	@Transactional(readOnly=true)
	public Page<ComponentDTO> listComponents(Pageable pageable) {
		List<ComponentDTO> componentListDTO = new ArrayList<ComponentDTO>();
		List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();
		String name, componentId, scope, format, implementation, rolesString;
		List<String> roles;
		for (Map<String, String> map : componentProperties) { // Retrieves all properties used in the output
			name = map.get(ComponentsConfig.FIELD_NAME);
			componentId = map.get(ComponentsConfig.FIELD_COMPONENT_ID);
			scope = map.get(ComponentsConfig.FIELD_SCOPE);
			format = map.get(ComponentsConfig.FIELD_FORMAT);
			implementation = map.get(ComponentsConfig.FIELD_IMPLEMENTATION);
			rolesString = map.get(ComponentsConfig.FIELD_ROLES);
			roles = new ArrayList<String>();
			if (rolesString != null) { // builds a list from the comma-separated string
				for (String s : rolesString.split(",")) {
					if (!s.trim().equals(""))
						roles.add(s.trim());
				}
			}
			
			componentListDTO.add(new ComponentDTO(name, componentId, scope, format, implementation, roles)); // adds the component to the output list
		}
		return new PageImpl<ComponentDTO>(componentListDTO, pageable, componentListDTO.size()); // returns as a page
	}
	
	/**
	 * Returns the possible roles for the requested component
	 * 
	 * @param componentId - ID of the component
	 * @return - A list of possible roles for the component
	 */
	public List<String> getComponentRoles(String componentId) {
		List<Map<String, String>> componentProperties = componentsConfiguration.getComponents();
		String rolesString;
		List<String> roles = new ArrayList<String>();
		for (Map<String, String> map : componentProperties) { // Retrieves all properties for each component
			if (componentId.equals(map.get(ComponentsConfig.FIELD_COMPONENT_ID))) { // it's the input component
				rolesString = map.get(ComponentsConfig.FIELD_ROLES);
				if (rolesString != null) { // builds a list from the comma-separated string
					for (String s : rolesString.split(",")) {
						if (!s.trim().equals(""))
							roles.add(s.trim());
					}
				}
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
	 */
	public List<ComponentConfigurationDTO> getConfigurations(Long organizationId) {
		Organization organization = organizationRepository.getOne(organizationId); // finds the organization
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		
		// Prepares the configuration, to show it as response. It will be a list with an element for each component.
		List<ComponentConfigurationDTO> config = new ArrayList<ComponentConfigurationDTO>();
		Map<String, ComponentConfigurationDTO> configMap = new HashMap<String, ComponentConfigurationDTO>();
		List<Tenant> tenants = tenantRepository.findByOrganization(organization); // the organization's tenants
		
		for (Tenant t : tenants) {
			String componentId = t.getTenantId().getComponentId();
			ComponentConfigurationDTO conf = configMap.get(componentId);
			if (conf == null) {
				conf = new ComponentConfigurationDTO(componentId, null);
				configMap.put(componentId, conf);
			}
			conf.addTenant(t.getTenantId().getName());
		}
		for (String s : configMap.keySet())
			config.add(configMap.get(s));
				
		return config;
	}
	
	/**
	 * Updates the organization's configuration.
	 * Input expects a list of component configurations. Each element must provide the component's ID
	 * and an array of the tenants that will belong to such component. Tenants previously present, but
	 * missing in the new configuration, will be deleted. Tenants previously present and also present
	 * in the new configurations will not be altered. New tenants will be created and assigned to the component.
	 * If the configuration for a specific component is not specified, its tenants will not be altered.
	 * 
	 * @param organizationId - Organization to configure
	 * @param configurationDTOList - Components whose tenants are to be updated
	 * @return - The full updated configuration of the organization
	 */
	public List<ComponentConfigurationDTO> updateConfigurations(Long organizationId, List<ComponentConfigurationDTO> configurationDTOList) {
		if (!utils.userHasAdminRights())
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		
		Organization organization = organizationRepository.getOne(organizationId);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		List<OrganizationMember> owners = listOwners(organization);
		if (owners == null || owners.isEmpty())
			throw new EntityNotFoundException("No owner of organization " + organization.getName() + " could be found, unable to update configuration.");
		
		List<Tenant> previousTenants = tenantRepository.findByOrganization(organization); // previous configuration
		List<Tenant> newTenants = validateConfiguration(organization, configurationDTOList); // new configuration
		
		List<Role> rolesToAdd = new ArrayList<Role>(); // new roles that will need to be added
		List<Role> rolesToRemove = new ArrayList<Role>(); // roles to be removed, due to an old tenant not present in the new configuration
		for (Tenant t : newTenants) { // loops on all tenants listed in the new configuration
			previousTenants.remove(t); // tenant is still in use
			for (OrganizationMember owner : owners)
				rolesToAdd.add(new Role(securityConfig.getOrganizationManagementContext() + "/" + t, Constants.ROLE_PROVIDER, owner, t.getTenantId().getComponentId())); // prepare role for this tenant
		}
		Set<String> componentIds = new HashSet<String>(); // component IDs that appear in the new configuration
		for (ComponentConfigurationDTO conf : configurationDTOList) {
			componentIds.add(conf.getComponentId());
		}
		List<Tenant> tenantsToRemove = new ArrayList<Tenant>(); // tenants no longer in use, to be removed
		for (Tenant t : previousTenants) {
			// Loops on tenants not present in the new configuration. If a tenant does not appear
			// because its component ID is not specified, however, it is interpreted as "leave
			// this component's configuration as it is".
			if (componentIds.contains(t.getTenantId().getComponentId())) { // component ID was specified, but the tenant is missing
				rolesToRemove.addAll(roleRepository.findByContextSpace(securityConfig.getOrganizationManagementContext() + "/" + t)); // tenant will be removed, so all related roles have to be removed
				tenantsToRemove.add(t); // tenant will have to be removed
			}
		}
		
		roleRepository.saveAll(rolesToAdd); // saves all new roles
		roleRepository.deleteAll(rolesToRemove); // removes roles for tenants no longer present in the components specified
		tenantRepository.saveAll(newTenants); // saves new tenants
		tenantRepository.deleteAll(tenantsToRemove); // deletes unused tenants
		
		// Updates roles on the identity provider
		if (!rolesToAdd.isEmpty()) {
			for (OrganizationMember owner : owners) {
				utils.idpAddRoles(owner.getIdpId(), rolesToAdd); // owner is given authority on all created tenants
			}
		}
		
		// Map that links each member to the roles that they must be stripped of
		Map<OrganizationMember, List<Role>> memberToRolesToRemove = new HashMap<OrganizationMember, List<Role>>();
		OrganizationMember member;
		for (Role r : rolesToRemove) {
			member = r.getOrganizationMember();
			List<Role> roles = memberToRolesToRemove.get(member);
			if (roles == null) {
				roles = new ArrayList<Role>();
				memberToRolesToRemove.put(member, roles);
			}
			roles.add(r);
		}
		for (OrganizationMember m : memberToRolesToRemove.keySet())
			utils.idpRemoveRoles(m.getIdpId(), memberToRolesToRemove.get(m)); // revokes roles in the identity provider
		
		// Updates tenants in the components
		Map<String, Component> componentMap = componentsModel.getListComponents();
		for (String s : componentMap.keySet()) {
			for (Tenant t : newTenants) {
				if (t.getTenantId().getComponentId().equals(s)) {
					for (OrganizationMember owner : owners)
						componentMap.get(s).createTenant(t.getTenantId().getName(), t.getOrganization().getName(), owner.getUsername());
				}
			}
			for (Tenant t : tenantsToRemove) {
				if (t.getTenantId().getComponentId().equals(s))
					componentMap.get(s).deleteTenant(t.getTenantId().getName(), t.getOrganization().getName());
			}
			for (Role r : rolesToAdd) {
				if (r.getComponentId().equals(s)) {
					for (OrganizationMember owner : owners)
						componentMap.get(s).assignRoleToUser(r.getSpaceRole(), organization.getName(), owner.getUsername());
				}
			}
			for (Role r : rolesToRemove) {
				if (r.getComponentId().equals(s)) {
					for (OrganizationMember owner : owners)
						componentMap.get(s).revokeRoleFromUser(r.getSpaceRole(), organization.getName(), owner.getUsername());
				}
			}
		}
		
		// Prepares the updated configuration, to show it as response. It will be a list with an element for each component.
		List<ComponentConfigurationDTO> updatedConfig = new ArrayList<ComponentConfigurationDTO>();
		Map<String, ComponentConfigurationDTO> updatedConfigMap = new HashMap<String, ComponentConfigurationDTO>();
		List<Tenant> updatedTenants = tenantRepository.findByOrganization(organization); // the organization's tenants after the update
		for (Tenant t : updatedTenants) {
			String componentId = t.getTenantId().getComponentId();
			ComponentConfigurationDTO conf = updatedConfigMap.get(componentId);
			if (conf == null) {
				conf = new ComponentConfigurationDTO(componentId, null);
				updatedConfigMap.put(componentId, conf);
			}
			conf.addTenant(t.getTenantId().getName());
		}
		for (String s : updatedConfigMap.keySet())
			updatedConfig.add(updatedConfigMap.get(s));
		
		return updatedConfig;
	}
	
	/**
	 * Validates the input configuration. Also returns the full list of tenants that appear in this new configuration.
	 * 
	 * @param organization - Organization to configure
	 * @param configurationDTOList - Components whose tenants are to be validated
	 * @return - Full list of all tenants appearing in the new configuration
	 */
	private List<Tenant> validateConfiguration(Organization organization, List<ComponentConfigurationDTO> configurationDTOList) {
		if (configurationDTOList == null || configurationDTOList.isEmpty())
			throw new IllegalArgumentException("No configuration specified.");
		List<Tenant> newTenants = new ArrayList<Tenant>();
		for (ComponentConfigurationDTO conf : configurationDTOList) { // loop on the input configurations
			if (conf == null || conf.getComponentId() == null)
				throw new IllegalArgumentException("The componentId field was not specified.");
			String componentId = conf.getComponentId();
			boolean componentIdFound = false;
			String componentIdProperty;
			for (Map<String, String> map : componentsConfiguration.getComponents()) { // checks that the component ID belongs to an actual component
				componentIdProperty = map.get(ComponentsConfig.FIELD_COMPONENT_ID);
				if (componentIdProperty != null && componentIdProperty.equals(componentId)) {
					componentIdFound = true; // component ID is valid
					break;
				}
			}
			if (!componentIdFound) // no component with the given ID could be found
				throw new IllegalArgumentException("Component " + componentId + " could not be found.");
			
			Pattern pattern = Pattern.compile("^[a-zA-Z0-9_-]+$"); // tenants need to have a certain format
			if (conf.getTenants() != null) {
				for (String t : conf.getTenants()) {
					if (!pattern.matcher(t).matches()) // tenant does not match the format
						throw new IllegalArgumentException("The following tenant contains illegal characters: " + t + ", please use only alphanumeric characters, dash (-) or underscore (_).");
					Tenant storedTenant = tenantRepository.findByComponentIdAndName(componentId, t); // check if tenant already exists
					if (storedTenant == null) { // tenant needs to be created
						storedTenant = new Tenant(componentId, t, organization);
					} else if (!storedTenant.getOrganization().getId().equals(organization.getId())) // cannot add this tenant
						throw new IllegalArgumentException("Tenant " + storedTenant + " is already in use by a different organization.");
					newTenants.add(storedTenant); // adds the tenant to the list of tenants of the new configuration
				}
			} else // component ID was specified, but the tenant field is missing
				throw new IllegalArgumentException("Component " + conf.getComponentId() + " was found, but the tenants field was"
						+ " absent or incomplete. If you meant to delete all tenants for this component, please specify the tenant"
						+ " field as an empty array. If you don't want to change the configuration of this component, remove its"
						+ " configuration entirely.");
		}
		return newTenants;
	}
	
	/**
	 * Lists all owners of the organization. Usually there's only 1, but there may be multiple owners in the future.
	 * 
	 * @param organization - Organization to list owners of
	 * @return - List of owners of the input organization
	 */
	private List<OrganizationMember> listOwners(Organization organization) {
		List<OrganizationMember> owners = new ArrayList<OrganizationMember>();
		// Retrieves the list of all roles that denote an owner
		List<Role> ownerRoles = roleRepository.findByContextSpaceAndRole(securityConfig.getOrganizationManagementContext() + "/" + organization.getSlug(), Constants.ROLE_PROVIDER);
		for (Role r : ownerRoles)
			owners.add(r.getOrganizationMember()); // adds the owner to the result
		return owners;
	}
}
