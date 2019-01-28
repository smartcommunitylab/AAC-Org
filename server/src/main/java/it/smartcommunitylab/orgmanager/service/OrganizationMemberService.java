package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;
import it.smartcommunitylab.orgmanager.model.Role;
import it.smartcommunitylab.orgmanager.model.Tenant;
import it.smartcommunitylab.orgmanager.repository.OrganizationMemberRepository;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;
import it.smartcommunitylab.orgmanager.repository.RoleRepository;
import it.smartcommunitylab.orgmanager.repository.TenantRepository;

@Service
@Transactional
public class OrganizationMemberService {

	@Autowired
	private OrganizationRepository organizationRepository;
	
	@Autowired
	private OrganizationMemberRepository organizationMemberRepository;
	
	@Autowired
	private TenantRepository tenantRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private OrgManagerUtils utils;
	
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
		Organization organization = organizationRepository.getOne(organizationId); // retrieves the organization
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		
		List<OrganizationMemberDTO> membersListDTO = new ArrayList<OrganizationMemberDTO>();
		List<OrganizationMember> members = organizationMemberRepository.findByOrganizationAndUsernameIgnoreCaseContaining(organization, username);
		Set<Role> memberRoles;
		for (OrganizationMember m : members) {
			memberRoles = roleRepository.findByOrganizationMember(m);
			membersListDTO.add(new OrganizationMemberDTO(m, memberRoles));
		}
		return membersListDTO;
	}
	
	/**
	 * User is granted the roles listed in the request body. Previously granted roles not present in this new configuration
	 * will be revoked.
	 * 
	 * @param organizationId - ID of the organization to add the member to
	 * @param memberDTO - Request body, contains the member to add and their roles
	 * @return - The updated member
	 */
	public OrganizationMemberDTO handleUserRoles(Long organizationId, OrganizationMemberDTO memberDTO) {
		Organization organization = organizationRepository.getOne(organizationId);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		if (memberDTO == null)
			return null;
		
		// Builds a collection of all of the organization's tenants
		Set<String> orgTenants = new HashSet<String>();
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		for (Tenant t : tenants)
			orgTenants.add("components/" + t.toString());
		
		// If the member is already present in the database, retrieves it
		OrganizationMember storedMember = organizationMemberRepository.findByUsernameAndOrganization(memberDTO.getUsername(), organization);
		if (storedMember == null) // member is new, create it
			storedMember = new OrganizationMember(memberDTO.getUsername(), organization); // converts from view format
		storedMember = organizationMemberRepository.save(storedMember); // stores member
		
		// Checks if roles are within the organization's tenants
		Set<RoleDTO> rolesDTO = memberDTO.getRoles();
		Set<Role> rolesToAdd = new HashSet<Role>();
		if (rolesDTO != null) {
			for (RoleDTO r : rolesDTO) {
				if (!orgTenants.contains(r.getContextSpace())) // role is not within the organization's tenants
					throw new IllegalArgumentException("The following role is not within the organization's tenants: " + r);
				rolesToAdd.add(new Role(r, storedMember)); // converts the role from view to model
			}
		}
		// Retrieves roles prior to this new configuration, to remove roles not present in the new configuration
		Set<Role> rolesToRemove = roleRepository.findByOrganizationMemberAndRoleNotIgnoreCase(storedMember, OrgManagerUtils.ROLE_PROVIDER);
		rolesToRemove.removeAll(rolesToAdd); // Roles not present in the new configuration
		
		String userId = utils.getUserId(storedMember.getUsername()); // ID used by the identity provider for the user
		
		// Checks that the authenticated user isn't trying to revoke owner role from themselves
//		String authenticatedId = utils.getAuthenticatedUserId(); // ID used by the identity provider for the authenticated user
//		if (userId.equals(authenticatedId)) {
//			for (Role r : rolesToRemove)
//				if (r.getRoleId().getRole().equals(OrgManagerUtils.ROLE_PROVIDER))
//					throw new IllegalArgumentException("You cannot revoke the role " + r + " from yourself.");
//		}
		roleRepository.saveAll(rolesToAdd); // Stores the member's new roles
		roleRepository.deleteAll(rolesToRemove); // Removes the roles not present in the new configuration
		
		// Updates roles in the identity provider
		utils.idpAddRoles(userId, rolesToAdd);
		utils.idpRemoveRoles(userId, rolesToRemove);
		
		// Creates user in the components
		Map<String, Component> componentMap = (Map<String, Component>) context.getBean("getComponents");
		boolean userCreated = false;
		for (String s : componentMap.keySet()) {
			for (Role r : rolesToAdd) {
				if (r.getComponentId().equals(s)) {
					if (!userCreated) {
						componentMap.get(s).createUser(storedMember.getUsername());
						userCreated = true;
					}
					componentMap.get(s).assignRoleToUser(r.getSpaceRole(), organization.getName(), storedMember.getUsername());
				}
			}
			for (Role r : rolesToRemove) {
				if (r.getComponentId() != null && r.getComponentId().equals(s))
					componentMap.get(s).revokeRoleFromUser(r.getSpaceRole(), organization.getName(), storedMember.getUsername());
			}
		}
		return new OrganizationMemberDTO(storedMember, roleRepository.findByOrganizationMember(storedMember)); // converts to view format
	}
	
	/**
	 * Removes a user from the organization.
	 * 
	 * @param organizationId - The organization the member will be removed from
	 * @param memberId - ID of the member to remove
	 */
	public void removeUser(Long organizationId, Long memberId) {
		Organization organization = organizationRepository.getOne(organizationId);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		
		// Checks if the member actually belongs to the organization
		OrganizationMember member = organizationMemberRepository.findByIdAndOrganization(memberId, organization); // retrieves the stored member
		if (member == null)
			throw new EntityNotFoundException("Organization with ID " + organizationId + " does not include a member with ID " + memberId + ": no changes were made.");
		
		String memberIdpId = utils.getUserId(member.getUsername()); // ID used by the identity provider for the member
		String authenticatedId = utils.getAuthenticatedUserId(); // ID used by the identity provider for the authenticated user
		if (memberIdpId.equals(authenticatedId))
			throw new IllegalArgumentException("You cannot remove yourself from the organization.");
		
		HashSet<Role> rolesToRemove = null;
		rolesToRemove = roleRepository.findByOrganizationMember(member);// roles the member has within the organization
		roleRepository.deleteAll(rolesToRemove);
		organizationMemberRepository.delete(member); // member can be removed from the organization
		
		// Updates roles in the identity provider
		utils.idpRemoveRoles(memberIdpId, rolesToRemove);
		
		// Removes the user for the components
		Map<String, Component> componentMap = (Map<String, Component>) context.getBean("getComponents");
		for (String s : componentMap.keySet())
			componentMap.get(s).removeUserFromOrganization(member.getUsername(), organization.getName());
	}
	
	/**
	 * Adds an owner to the organization.
	 * 
	 * @param organizationId - ID of the organization to add an owner to
	 * @param ownerDTO - Request body, contains the name of the new owner
	 * @return - The new owner of the organization
	 */
	public OrganizationMemberDTO addOwner(Long organizationId, OrganizationMemberDTO ownerDTO) {
		Organization organization = organizationRepository.getOne(organizationId);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		
		// Checks if the user has permission to perform this action
		if (!utils.userHasAdminRights() && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		if (ownerDTO == null)
			return null;
		
		String ownerName = ownerDTO.getUsername();
		OrganizationMember owner = organizationMemberRepository.findByUsernameAndOrganization(ownerName, organization);
		HashSet<Role> roles = new HashSet<Role>();
		if (owner == null) // owner user needs to be created
			owner = new OrganizationMember(ownerName, organization);
		else // new owner is an existing user
			roles = roleRepository.findByOrganizationMember(owner); // retrieve the roles for output
		
		Role ownerRole = new Role(OrgManagerUtils.ROOT_ORGANIZATIONS + "/" + organization.getSlug(), OrgManagerUtils.ROLE_PROVIDER, owner, null);
		List<Role> rolesToAdd = new ArrayList<Role>();
		rolesToAdd.add(ownerRole); // owner role
		// New owner must also be given ROLE_PROVIDER role on all tenants of the organization
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		for (Tenant t : tenants)
			rolesToAdd.add(new Role(OrgManagerUtils.ROOT_COMPONENTS + "/" + t.getTenantId().getComponentId() + "/" + t.getTenantId().getName(),
					OrgManagerUtils.ROLE_PROVIDER, owner, t.getTenantId().getComponentId()));
		organizationMemberRepository.save(owner);
		roleRepository.saveAll(rolesToAdd);
		
		// Updates roles in the identity provider
		String ownerId = utils.getUserId(ownerName);
		utils.idpAddRoles(ownerId, rolesToAdd);
		
		// Adds the owner for the components
		Map <String, Component> componentMap = (Map<String, Component>) context.getBean("getComponents");
		for (String s : componentMap.keySet()) {
			componentMap.get(s).createUser(ownerName);
			componentMap.get(s).addOwner(ownerName, organization.getName());
		}
		
		roles.addAll(rolesToAdd); // adds all new roles to the output roles
		return new OrganizationMemberDTO(owner, roles);
	}
	
	/**
	 * Removes an owner from the organization.
	 * 
	 * @param organizationId - The organization the owner will be removed from
	 * @param ownerId - ID of the owner to remove
	 */
	public void removeOwner(Long organizationId, Long ownerId) {
		Organization organization = organizationRepository.getOne(organizationId);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		
		// Checks if the user has permission to perform this action
		boolean userHasAdminRights = utils.userHasAdminRights();
		if (!userHasAdminRights && !utils.userIsOwner(organization))
			throw new AccessDeniedException("Access is denied: user does not have administrator rights.");
		
		OrganizationMember owner = organizationMemberRepository.findByIdAndOrganization(ownerId, organization); // retrieves the stored member
		if (owner == null) // The input user does not belong to the organization
			throw new EntityNotFoundException("There is no user in organization " + organization.getName() + " with ID " + ownerId);
		
		String ownerName = owner.getUsername();
		String ownerIdpId = utils.getUserId(ownerName); // ID used by the identity provider
		
		if (!userHasAdminRights && utils.getAuthenticatedUserId().equals(ownerIdpId)) // authenticated user is trying to remove themselves
			throw new IllegalArgumentException("You are owner of the organization and are trying to remove your own owner status.");
		
		Set<Role> roles = roleRepository.findByOrganizationMember(owner);
		List<Role> rolesToRemove = new ArrayList<Role>();
		Role ownerRole = new Role(OrgManagerUtils.ROOT_ORGANIZATIONS + "/" + organization.getSlug(), OrgManagerUtils.ROLE_PROVIDER, owner, null);
		if (!roles.contains(ownerRole)) // The input user is not owner of the organization
			throw new EntityNotFoundException("User " + ownerName + " belongs to organization " + organization.getName() + ", but is not owner of it.");
		rolesToRemove.add(ownerRole); // owner role
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		// Former owner must also be revoked ROLE_PROVIDER role for all tenants of the organization
		for (Tenant t : tenants)
			rolesToRemove.add(new Role(OrgManagerUtils.ROOT_COMPONENTS + "/" + t.getTenantId().getComponentId() + "/" + t.getTenantId().getName(),
					OrgManagerUtils.ROLE_PROVIDER, owner, t.getTenantId().getComponentId()));
		roleRepository.deleteAll(rolesToRemove);
		
		// Updates roles in the identity provider
		utils.idpRemoveRoles(ownerIdpId, rolesToRemove);
		
		// Removes the owner for the components
		Map <String, Component> componentMap = (Map<String, Component>) context.getBean("getComponents");
		for (String s : componentMap.keySet())
			componentMap.get(s).removeOwner(ownerName, organization.getName());
	}
}
