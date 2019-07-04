package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;
import it.smartcommunitylab.orgmanager.config.SecurityConfig;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.dto.UserRightsDTO;
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
	private OrgManagerUtils utils;
	
	@Autowired
	private SecurityConfig securityConfig;
	
	@Autowired
	private ComponentsModel componentsModel;
	
	private Log log = LogFactory.getLog(OrganizationMemberService.class);
	
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
		List<Object[]> memberRolesList = roleRepository.findOrganizationMembersWithRoles(organizationId, username);
		Map<OrganizationMember, List<Role>> memberRolesMap = utils.createMemberToRolesMap(memberRolesList);

		for (OrganizationMember m : memberRolesMap.keySet())
			membersListDTO.add(new OrganizationMemberDTO(m, memberRolesMap.get(m)));
		return membersListDTO;
	}
	
	/**
	 * Returns an object that indicates whether or not the authenticated user has administrator rights and a list of IDs of organizations the user is owner of.
	 * 
	 * @return - Object that contains the authenticated user's rights
	 */
	public UserRightsDTO getUserRights() {
		return new UserRightsDTO(utils.getAuthenticatedUserName(), utils.userHasAdminRights(),
				organizationMemberRepository.findOwnedOrganizations(utils.getAuthenticatedUserId()));
	}
	
	/**
	 * User is granted the roles listed in the request body. Previously granted roles not present in this new configuration will be revoked.
	 * 
	 * @param organizationId - ID of the organization to add the member to
	 * @param memberDTO - Request body, contains the member to add and their roles
	 * @return - The updated member
	 */
	public OrganizationMemberDTO handleUserRoles(Long organizationId, OrganizationMemberDTO memberDTO) {
		Organization organization = organizationRepository.getOne(organizationId);
		organization.toString(); // sometimes, even if the organization is not found, getOne will not return null: this line will make it throw EntityNotFoundException
		
		// Checks if the user has permission to perform this action
		boolean authAsAdmin = utils.userHasAdminRights();
		if (!authAsAdmin) {
			OrganizationMember authenticatedUser = organizationMemberRepository.findByIdpIdAndOrganization(utils.getAuthenticatedUserId(), organization);
			if (!authenticatedUser.getOwner())
				throw new AccessDeniedException("Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
		}
		
		// Builds a collection of all of the organization's tenants
		List<String> orgTenants = new ArrayList<String>();
		List<Tenant> tenants = tenantRepository.findByOrganization(organization);
		List<String> tenantNames = new ArrayList<String>(); // List of just the names of the organization's tenants, used for some connectors
		for (Tenant t : tenants) {
			orgTenants.add(Constants.ROOT_COMPONENTS + "/" + t.toString());
			tenantNames.add(t.getTenantId().getName());
		}
		
		// Checks that the member's name was specified
		if (memberDTO == null || memberDTO.getUsername() == null || memberDTO.getUsername().trim().equals(""))
			throw new IllegalArgumentException("No user name specified.");
		
		// If the member is already present in the database, retrieves it
		String userName = memberDTO.getUsername().trim();
		OrganizationMember storedMember = organizationMemberRepository.findByUsernameAndOrganization(userName, organization);
		Long userIdpId; // ID used by the identity provider for the user
		boolean isOwner = memberDTO.getOwner();
		
		if (storedMember == null) { // member is new, create it
			userIdpId = utils.getUserId(userName); // retrieves the ID from the identity provider
			if (!authAsAdmin) // if the calling user does not have administrator rights, they cannot grant owner status
				isOwner = false;
			storedMember = new OrganizationMember(userName, organization, userIdpId, isOwner);
		} else {
			userIdpId = storedMember.getIdpId();
			if (!authAsAdmin)
				isOwner = storedMember.getOwner(); // if the calling user does not have administrator rights, owner status cannot be changed
			storedMember.setOwner(isOwner); // Updates owner status; does not have any effect if the calling user does not have administrator rights
		}
		storedMember = organizationMemberRepository.save(storedMember); // stores member
		
		Set<RoleDTO> rolesDTO = memberDTO.getRoles();
		Set<Role> rolesToAdd = new HashSet<Role>(); // roles to grant
		Set<Role> rolesToDel = roleRepository.findByOrganizationMemberAndRoleNotIgnoreCase(storedMember, Constants.ROLE_PROVIDER); // roles to revoke
		
		// Builds set of owner-related roles; which will be either granted or revoked based on whether the user was marked as owner or not
		Set<Role> ownerRoles = new HashSet<Role>();
		// Owners have ROLE_PROVIDER role on the organization
		Role ownerRole = new Role(securityConfig.getOrganizationManagementContext() + "/" + organization.getSlug(),
				Constants.ROLE_PROVIDER, storedMember, null);
		ownerRoles.add(ownerRole);
		// Owners have ROLE_PROVIDER role on all tenants of the organization
		for (Tenant t : tenants) {
			Role tenantRole = new Role(Constants.ROOT_COMPONENTS + "/" + t.getTenantId().getComponentId() + "/" + t.getTenantId().getName(),
					Constants.ROLE_PROVIDER, storedMember, t.getTenantId().getComponentId());
			ownerRoles.add(tenantRole);
		}
		
		// Builds set of non-owner-related (regular) roles that the request wants the member to have
		if (rolesDTO != null) {
			for (RoleDTO r : rolesDTO) {
				if (!r.getRole().equals(Constants.ROLE_PROVIDER)) {
					if (!orgTenants.contains(r.getContextSpace())) // role is not within the organization's tenants
						throw new IllegalArgumentException("The following role is not within the organization's tenants: " + r);
					rolesToAdd.add(new Role(r, storedMember)); // converts the role from view to model
				}
			}
		}
		rolesToDel.removeAll(rolesToAdd); // all regular roles present in the request should not be revoked
		
		roleRepository.saveAll(rolesToAdd); // grants all regular roles present in the request
		roleRepository.deleteAll(rolesToDel); // revokes all previously held regular roles that are not present in the new configuration
		
		if (isOwner) // if the member is owner, all owner-related roles are granted
			roleRepository.saveAll(ownerRoles);
		else // if the member is not owner, all owner-related roles are revoked
			roleRepository.deleteAll(ownerRoles);
		
		// If the user no longer has any roles within the organization, they are removed from it
		Set<Role> updatedRoles = roleRepository.findByOrganizationMember(storedMember);
		boolean removeUser = false;
		if (updatedRoles.isEmpty()) { // no roles remaining
			organizationMemberRepository.delete(storedMember);
			removeUser = true;
		}
		
		// Updates roles in the identity provider
		utils.idpAddRoles(userIdpId, rolesToAdd);
		utils.idpRemoveRoles(userIdpId, rolesToDel);
		if (isOwner) // updates owner-related roles
			utils.idpAddRoles(userIdpId, ownerRoles);
		else
			utils.idpRemoveRoles(userIdpId, ownerRoles);
		
		// Handles user in the components
		Map<String, Component> componentMap = componentsModel.getListComponents();
		UserInfo userInfo = utils.getIdpUserDetails(userName);
		String resultMessage;
		for (String s : componentMap.keySet()) {
			
			// Create the user; components are supposed to not do anything if the user already exists
			resultMessage = componentMap.get(s).createUser(userInfo);
			if(CommonUtils.isErroneousResult(resultMessage))
				throw new EntityNotFoundException(resultMessage);
			
			// Grant/revoke owner status to the user
			if (isOwner) {
				resultMessage = componentMap.get(s).addOwner(userInfo, organization.getName());
				if(CommonUtils.isErroneousResult(resultMessage))
					throw new EntityNotFoundException(resultMessage);
			} else {
				resultMessage = componentMap.get(s).removeOwner(userInfo, organization.getName());
				if(CommonUtils.isErroneousResult(resultMessage))
					throw new EntityNotFoundException(resultMessage);
			}
			
			// Grant all roles present in the new configuration
			for (Role r : rolesToAdd) {
				if (r.getComponentId().equals(s)) {
					resultMessage = componentMap.get(s).assignRoleToUser(r.getSpaceRole(), organization.getName(), userInfo);
					if(CommonUtils.isErroneousResult(resultMessage))
						throw new EntityNotFoundException(resultMessage);
				}
			}
			
			// Revoke all roles missing in the new configuration
			for (Role r : rolesToDel) {
				if (r.getComponentId() != null && r.getComponentId().equals(s)) {
					resultMessage = componentMap.get(s).revokeRoleFromUser(r.getSpaceRole(), organization.getName(), userInfo);
					if(CommonUtils.isErroneousResult(resultMessage))
						throw new EntityNotFoundException(resultMessage);
				}
			}
			
			// Remove user if no roles remain
			if (removeUser)
				componentMap.get(s).removeUserFromOrganization(userInfo, organization.getName(), tenantNames);
		}
		
		return new OrganizationMemberDTO(storedMember, updatedRoles);
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
		OrganizationMember member = organizationMemberRepository.getOne(memberId); // retrieves the stored member
		if (member == null)
			throw new EntityNotFoundException("Organization with ID " + organizationId + " does not include a member with ID " + memberId + ": no changes were made.");
		
		Long memberIdpId = member.getIdpId(); // ID used by the identity provider for the member
		Long authenticatedId = utils.getAuthenticatedUserId(); // ID used by the identity provider for the authenticated user
		if (memberIdpId.equals(authenticatedId))
			throw new IllegalArgumentException("You cannot remove yourself from the organization.");
		
		HashSet<Role> rolesToRemove = null;
		rolesToRemove = roleRepository.findByOrganizationMember(member);// roles the member has within the organization
		roleRepository.deleteAll(rolesToRemove);
		organizationMemberRepository.delete(member); // member can be removed from the organization
		
		// Updates roles in the identity provider
		utils.idpRemoveRoles(memberIdpId, rolesToRemove);
		
		List<String> tenantsList = new ArrayList<String>();
		for (Tenant t : tenantRepository.findByOrganization(organization))
			tenantsList.add(t.getTenantId().getName());
		
		// Removes the user for the components
		Map<String, Component> componentMap = componentsModel.getListComponents();
		for (String s : componentMap.keySet()) {
			String resultMessage = componentMap.get(s).removeUserFromOrganization(utils.getIdpUserDetails(member.getUsername()), organization.getName(), tenantsList);
			if(CommonUtils.isErroneousResult(resultMessage))
				throw new EntityNotFoundException(resultMessage);
		}
	}
}
