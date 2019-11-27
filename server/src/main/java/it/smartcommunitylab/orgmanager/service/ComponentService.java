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
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
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
@Transactional(rollbackFor=Exception.class)
public class ComponentService {
	
	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private TenantRepository tenantRepository;
	
	@Autowired
	private ComponentsConfiguration componentsConfiguration;
	
	@Autowired
	private ComponentsModel componentsModel;
	
	@Autowired
	private OrgManagerUtils utils;
	
	@Autowired
	private RoleService roleService;

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
			name = map.get(Constants.FIELD_NAME);
			componentId = map.get(Constants.FIELD_COMPONENT_ID);
			scope = map.get(Constants.FIELD_SCOPE);
			format = map.get(Constants.FIELD_FORMAT);
			implementation = map.get(Constants.FIELD_IMPLEMENTATION);
			rolesString = map.get(Constants.FIELD_ROLES);
			roles = parseRoles(rolesString);
			componentListDTO.add(new ComponentDTO(name, componentId, scope, format, implementation, roles)); // adds the component to the output list
		}
		return new PageImpl<ComponentDTO>(componentListDTO, pageable, componentListDTO.size()); // returns as a page
	}

	protected List<String> parseRoles(String rolesString) {
		if (StringUtils.isEmpty(rolesString)) return Collections.emptyList(); 
		return StringUtils.commaDelimitedListToSet(rolesString).stream().map(r -> r.trim()).filter(r -> !"".equals(r)).collect(Collectors.toList());
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
			if (componentId.equals(map.get(Constants.FIELD_COMPONENT_ID))) { // it's the input component
				rolesString = map.get(Constants.FIELD_ROLES);
				return parseRoles(rolesString);
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
		Organization organization = organizationRepository.findById(organizationId).orElse(null); // finds the organization
		
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug()))
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
		
		Organization organization = organizationRepository.findById(organizationId).orElse(null); // finds the organization

		Set<User> owners = roleService.getOrganizationOwners(organization.getSlug());
		
		if (owners == null || owners.isEmpty())
			throw new EntityNotFoundException("No owner of organization " + organization.getName() + " could be found, unable to update configuration.");
		
		List<Tenant> previousTenants = tenantRepository.findByOrganization(organization); // previous configuration
		List<Tenant> newTenants = validateConfiguration(organization, configurationDTOList); // new configuration
		
		
		Set<User> rolesToAdd = new HashSet<>(); // new roles that will need to be added
		Set<User> rolesToRemove = new HashSet<>(); // roles to be removed, due to an old tenant not present in the new configuration
		
		for (Tenant t : newTenants) { // loops on all tenants listed in the new configuration
			previousTenants.remove(t); // tenant is still in use
			for (User owner : owners) {
				owner.getRoles().add(AACRoleDTO.tenantOwner(t.getTenantId().getComponentId(), t.getTenantId().getName()));
				owner.getRoles().add(AACRoleDTO.orgMember(organization.getSlug()));
				rolesToAdd.add(owner);
			}
		}
		Set<String> componentIds = configurationDTOList.stream().map(ComponentConfigurationDTO::getComponentId).collect(Collectors.toSet());
				
		List<Tenant> tenantsToRemove = new ArrayList<Tenant>(); // tenants no longer in use, to be removed
		for (Tenant t : previousTenants) {
			// Loops on tenants not present in the new configuration. If a tenant does not appear
			// because its component ID is not specified, however, it is interpreted as "leave
			// this component's configuration as it is".
			if (componentIds.contains(t.getTenantId().getComponentId())) { // component ID was specified, but the tenant is missing
				Role role = AACRoleDTO.tenantUser(t.getTenantId().getComponentId(), t.getTenantId().getName());
				Set<User> toRemove = roleService.getRoleUsers(role.canonicalSpace());
				rolesToRemove.addAll(toRemove);
				tenantsToRemove.add(t);
			}
		}		
		tenantRepository.saveAll(newTenants); // saves new tenants
		tenantRepository.deleteAll(tenantsToRemove); // deletes unused tenants
		
		roleService.addRoles(rolesToAdd);
		roleService.deleteRoles(rolesToRemove);
		
		Map<String, Component> componentMap = componentsModel.getListComponents();
		for (Tenant t : newTenants) { // Creates new tenants
			Component component = componentMap.get(t.getTenantId().getComponentId());
			if (component != null) {
				Organization org = t.getOrganization();
				UserInfo userInfo = new UserInfo(org.getContactsEmail(), org.getContactsName(), org.getContactsSurname());
				String resultMessage = component.createTenant(t.getTenantId().getName(), t.getOrganization().getName(), userInfo);
				if(CommonUtils.isErroneousResult(resultMessage)) {
					throw new EntityNotFoundException(resultMessage);
				}
			}
		}
		for (Tenant t : tenantsToRemove) {// Deletes tenants no longer in use
			Component component = componentMap.get(t.getTenantId().getComponentId());
			String resultMessage = component.deleteTenant(t.getTenantId().getName(), t.getOrganization().getName());
			if(CommonUtils.isErroneousResult(resultMessage)) {
				throw new EntityNotFoundException(resultMessage);
			}
		}
		return getConfigurations(organizationId);
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
			String componentTenantPattern = "";
			for (Map<String, String> map : componentsConfiguration.getComponents()) { // checks that the component ID belongs to an actual component
				componentIdProperty = map.get(Constants.FIELD_COMPONENT_ID);
				if (componentIdProperty != null && componentIdProperty.equals(componentId)) {
					componentIdFound = true; // component ID is valid
					componentTenantPattern = map.get(Constants.FIELD_FORMAT);
					break;
				}
			}
			if (!componentIdFound) // no component with the given ID could be found
				throw new IllegalArgumentException("Component " + componentId + " could not be found.");
			Pattern pattern = Pattern.compile(componentTenantPattern); // tenants need to have a certain format
			if (conf.getTenants() != null) {
				for (String t : conf.getTenants()) {
					if (!pattern.matcher(t).matches()) // tenant does not match the format
						throw new IllegalArgumentException("The following tenant contains illegal characters: " + t + ", please match this regex: " + componentTenantPattern);
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
}
