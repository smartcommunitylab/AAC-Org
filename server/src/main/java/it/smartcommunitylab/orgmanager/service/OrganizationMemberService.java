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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.dto.UserRightsDTO;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.Tenant;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;
import it.smartcommunitylab.orgmanager.repository.TenantRepository;

@Service
@Transactional(rollbackFor=Exception.class)
public class OrganizationMemberService {

	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private TenantRepository tenantRepository;
	
	@Autowired
	private OrgManagerUtils utils;
	
	@Autowired
	private ComponentsModel componentsModel;
	
	@Autowired
	private RoleService roleService;
	/**
	 * Lists users within an organization.
	 * 
	 * @param organizationId - ID of the organization
	 * @param username - If specified, only users whose name contains this string will be returned
	 * @return - Users in the organization
	 */
	public List<OrganizationMemberDTO> getUsers(Long organizationId, String username) {
		if (username == null)
			username = ""; // null is not accepted, but an empty string works just fine when a filter on the name is not desired
		Organization organization = organizationRepository.findById(organizationId).orElse(null); // finds the organization
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug()))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		
		Set<User> users = roleService.getOrganizationMembers(organization.getSlug());
		String lcname = username.toLowerCase();
		users = users.stream()
				.filter(u -> u.getFullname().toLowerCase().contains(lcname))
				.collect(Collectors.toSet());
		
		filterRoles(users, organization);
		return users.stream().map(u -> new OrganizationMemberDTO(u)).collect(Collectors.toList());
	}
	
	private void filterRoles(Set<User> users, Organization organization) {
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		Set<String> orgTenantSpaces = tenants.stream().map(t -> AACRoleDTO.tenantUser(t.getTenantId().getComponentId(), t.getTenantId().getName()).canonicalSpace()).collect(Collectors.toSet());
		Set<String> orgSpaces = new HashSet<>(orgTenantSpaces);
		orgSpaces.add(AACRoleDTO.orgMember(organization.getSlug()).canonicalSpace());
		users.forEach(m -> {
			Set<Role> roles = roleService.getRoles(m).stream().filter(r -> orgSpaces.contains(r.canonicalSpace())).collect(Collectors.toSet());
			m.setRoles(roles);
		});
	}
	
	/**
	 * Returns an object that indicates whether or not the authenticated user has administrator rights and a list of IDs of organizations the user is owner of.
	 * 
	 * @return - Object that contains the authenticated user's rights
	 */
	public UserRightsDTO getUserRights() {
		return new UserRightsDTO(utils.getAuthenticatedUserName(), utils.userHasAdminRights(), utils.findOwnedOrganizations());
	}
	
	/**
	 * User is granted the roles listed in the request body. Previously granted roles not present in this new configuration will be revoked.
	 * 
	 * @param organizationId - ID of the organization to add the member to
	 * @param memberDTO - Request body, contains the member to add and their roles
	 * @return - The updated member
	 */
	public OrganizationMemberDTO handleUserRoles(Long organizationId, OrganizationMemberDTO memberDTO) {
		Organization organization = organizationRepository.findById(organizationId).orElse(null); // finds the organization
		
		// Checks if the user has permission to perform this action
		boolean authAsAdmin = utils.userHasAdminRights();
		if (!authAsAdmin) {
			if (!utils.userIsOwner(organization.getSlug()))
				throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		}
		
		// Checks that the member's name was specified
		if (memberDTO == null || memberDTO.getUsername() == null || memberDTO.getUsername().trim().equals(""))
			throw new IllegalArgumentException("No user name specified.");
		
		BasicProfile profile = utils.getIdpUserProfile(memberDTO.getUsername());
		if (profile == null) {
			throw new IllegalArgumentException("User does not exist.");
		}
		memberDTO.setId(profile.getUserId());
		
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		// Builds a collection of all of the organization's tenants
		Set<String> orgTenants = new HashSet<String>();
		List<String> tenantNames = new ArrayList<String>(); // List of just the names of the organization's tenants, used for some connectors
		for (Tenant t : tenants) {
			orgTenants.add(Constants.ROOT_COMPONENTS + "/" + t.toString());
			tenantNames.add(t.getTenantId().getName());
		}
		
		Set<RoleDTO> rolesDTO = memberDTO.getRoles();
		User user = new User(); user.setUserId(memberDTO.getId()); user.setUsername(memberDTO.getUsername());
		Set<Role> oldRoles = roleService.getRoles(user);
		Set<Role> rolesToAdd = new HashSet<>(); // roles to grant
		Set<Role> rolesToDel = new HashSet<>(oldRoles); // roles to delete
		Map<String, Set<Role>> componentRolesToAdd = new HashMap<>();
		Map<String, Set<Role>> componentRolesToRemove = new HashMap<>();
		Map<String, Set<Role>> componentRolesToKeep = new HashMap<>();
		oldRoles.forEach(r -> {
			if (AACRoleDTO.isComponentRole(r) && orgTenants.contains(r.canonicalSpace())) {
				String component = AACRoleDTO.componentName(r);
				Set<Role> set = componentRolesToRemove.getOrDefault(component, new HashSet<>());
				set.add(r);
				componentRolesToRemove.put(component, set);
			} else {
				rolesToDel.remove(r);
			}
		});
		
		for (RoleDTO r : rolesDTO) {
			Role aacRole = AACRoleDTO.from(r);
			// cannot add roles for tenants non belonging to the organization
			if (AACRoleDTO.isComponentRole(aacRole) && !orgTenants.contains(aacRole.canonicalSpace())) {
				throw new IllegalArgumentException("The following role is not within the organization's tenants: " + r);
			}
			if (AACRoleDTO.isComponentRole(aacRole)) {
				String component = AACRoleDTO.componentName(aacRole);
				// not changed, keep role in component
				if (componentRolesToRemove.containsKey(component) && componentRolesToRemove.get(component).contains(aacRole)) {
					componentRolesToRemove.get(component).remove(aacRole);
					Set<Role> set = componentRolesToKeep.getOrDefault(component, new HashSet<>());
					set.add(aacRole);
					componentRolesToKeep.put(component, set);
				} else {
					Set<Role> set = componentRolesToAdd.getOrDefault(component, new HashSet<>());
					set.add(aacRole);
					componentRolesToAdd.put(component, set);
				}
				rolesToDel.remove(aacRole);
				rolesToAdd.add(aacRole);
			}
		};
		
		if (memberDTO.getOwner()) {
			rolesToAdd.add(AACRoleDTO.orgOwner(organization.getSlug()));
		} else {
			Role orgOwner = AACRoleDTO.orgOwner(organization.getSlug());
			rolesToAdd.remove(orgOwner);
			rolesToAdd.add(AACRoleDTO.orgMember(organization.getSlug()));
			rolesToDel.add(orgOwner);
		}

		// if any role is assigned, keep the org membership 
		if (rolesToAdd.size() > 0) {
			rolesToDel.remove(AACRoleDTO.orgMember(organization.getSlug()));
		}
		
		User toUpdate = new User(); toUpdate.setUserId(memberDTO.getId()); toUpdate.setUsername(memberDTO.getUsername());
		toUpdate.setRoles(rolesToDel);
		if (rolesToDel.size() > 0) roleService.deleteRoles(Collections.singleton(toUpdate));
		toUpdate.setRoles(rolesToAdd);
		if (rolesToAdd.size() > 0) roleService.addRoles(Collections.singleton(toUpdate));
		
		UserInfo userInfo = new UserInfo(profile.getUsername(), profile.getName(), profile.getSurname());
		// Handles user in the components
		Map<String, Component> componentMap = componentsModel.getListComponents();
		String resultMessage;
		for (String s : componentMap.keySet()) {
			// new roles to add
			if (componentRolesToAdd.containsKey(s)) {
				// Create the user; components are supposed to not do anything if the user already exists
				resultMessage = componentMap.get(s).createUser(userInfo);
				if(CommonUtils.isErroneousResult(resultMessage))
					throw new EntityNotFoundException(resultMessage);
				
				Set<Role> added = componentRolesToAdd.get(s);
				for (Role r : added) {
					resultMessage = componentMap.get(s).assignRoleToUser(r.getSpace()+":"+r.getRole(), organization.getName(), userInfo);
					if(CommonUtils.isErroneousResult(resultMessage))
						throw new EntityNotFoundException(resultMessage);
				}
			// remove user as no roles exist	
			} else if (!componentRolesToKeep.containsKey(s)){
				resultMessage= componentMap.get(s).removeUserFromOrganization(userInfo, organization.getName(), tenantNames);
				if(CommonUtils.isErroneousResult(resultMessage))
					throw new EntityNotFoundException(resultMessage);
			// remove roles that are not used
			} else {
				Set<Role> removed = componentRolesToRemove.get(s);
				for (Role r : removed) {
					resultMessage = componentMap.get(s).revokeRoleFromUser(r.getSpace()+":"+r.getRole(), organization.getName(), userInfo);
					if(CommonUtils.isErroneousResult(resultMessage))
						throw new EntityNotFoundException(resultMessage);
				}
				
			}
		}

		user.setRoles(roleService.getRoles(user));
		filterRoles(Collections.singleton(user), organization);
		return new OrganizationMemberDTO(user);
	}
	
	/**
	 * Removes a user from the organization.
	 * 
	 * @param organizationId - The organization the member will be removed from
	 * @param memberId - ID of the member to remove
	 */
	public void removeUser(Long organizationId, String memberId) {
		Organization organization = organizationRepository.findById(organizationId).orElse(null); // finds the organization
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug()))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");

		String authenticatedId = utils.getAuthenticatedUserId(); // ID used by the identity provider for the authenticated user
		if (memberId.equals(authenticatedId))
			throw new IllegalArgumentException("You cannot remove yourself from the organization.");
		
		User user = new User(); user.setUserId(memberId);
		BasicProfile profile = utils.getIdpUserProfileById(memberId);
		UserInfo userInfo = new UserInfo(profile.getUsername(), profile.getName(), profile.getSurname());
		Set<Role> roles = roleService.getRoles(user);
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		List<String> tenantNames = new LinkedList<>();
		Set<String> orgSpaces = new HashSet<String>(); // List of just the names of the organization's tenants, used for some connectors
		for (Tenant t : tenants) {
			orgSpaces.add(Constants.ROOT_COMPONENTS + "/" + t.toString());
			tenantNames.add(t.getTenantId().getName());
		}
		orgSpaces.add(AACRoleDTO.orgMember(organization.getSlug()).canonicalSpace());
		roles = roles.stream().filter(r -> orgSpaces.contains(r.canonicalSpace())).collect(Collectors.toSet());
		
		user.setRoles(roles);
		roleService.deleteRoles(Collections.singleton(user));

		// Removes the user for the components
		Map<String, Component> componentMap = componentsModel.getListComponents();
		for (String s : componentMap.keySet()) {
			String resultMessage = componentMap.get(s).removeUserFromOrganization(userInfo, organization.getName(), tenantNames);
			if(CommonUtils.isErroneousResult(resultMessage))
				throw new EntityNotFoundException(resultMessage);
		}
	}
}
