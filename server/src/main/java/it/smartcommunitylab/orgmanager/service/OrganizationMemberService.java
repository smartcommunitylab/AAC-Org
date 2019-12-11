package it.smartcommunitylab.orgmanager.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;

@Service
@Transactional(readOnly = true)
public class OrganizationMemberService {
    private final static Logger logger = LoggerFactory.getLogger(OrganizationMemberService.class);

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private ComponentService componentService;

    /**
     * Lists users within an organization.
     * 
     * @param organizationId - ID of the organization
     * @return - Users in the organization
     * @throws IdentityProviderAPIException
     */
    public List<OrganizationMemberDTO> getUsers(long organizationId)
            throws SystemException {

        logger.debug("list users for organization " + String.valueOf(organizationId) );

        try {
            // find the organization
            Organization organization = organizationRepository.findById(organizationId).orElse(null);
            if (!OrgManagerUtils.userHasAdminRights() && !OrgManagerUtils.userIsOwner(organization.getSlug())) {
                throw new AccessDeniedException(
                        "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
            }

            // get members from role service
            Set<User> users = roleService.getOrganizationMembers(organization.getSlug());

            filterRoles(users, organization);
            return users.stream().map(u -> OrganizationMemberDTO.from(u)).collect(Collectors.toList());
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }

    private void filterRoles(Set<User> users, Organization organization) throws IdentityProviderAPIException {
		for (User m : users) {
			m.setRoles(roleService.getRoles(m, organization.getSlug()));
		}
    }

    /**
     * User is granted the roles listed in the request body. Previously granted
     * roles not present in this new configuration will be revoked.
     * 
     * @param organizationId - ID of the organization to add the member to
     * @param owner 
     * @param memberDTO      - Request body, contains the member to add and their
     *                       roles
     * @return - The updated member
     * @throws SystemException
     * @throws NoSuchOrganizationException
     * @throws InvalidArgumentException
     * @throws NoSuchUserException
     */
    public OrganizationMemberDTO handleUserRoles(Long organizationId, String userName, Set<RoleDTO> roles, boolean owner)
            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException {

        // finds the organization
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        // Checks if the user has permission to perform this action
        if (!OrgManagerUtils.userHasAdminRights() && !OrgManagerUtils.userIsOwner(organization.getSlug())) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        // Checks that the member's name was specified
        if (userName == null || userName.trim().equals("")) {
            throw new InvalidArgumentException("No user name specified.");
        }

        logger.info("update roles for user " + userName + " from organization " + String.valueOf(organizationId));

        try {
            BasicProfile profile = profileService.getUserProfile(userName);
            if (profile == null) {
                logger.error("user " + userName + " does not exists");
                throw new InvalidArgumentException("The user does not exists: " + userName);
            }

            String userId = profile.getUserId();
            
            User user = new User();
            user.setUserId(userId);
            user.setUsername(userName);
            
            Set<Role> allRoles = roleService.getRoles(user, organization.getSlug());
            
            Set<Role> oldRoles = allRoles.stream().filter(r -> !AACRoleDTO.isOrgRole(r)).collect(Collectors.toSet());
	        Set<Role> rolesToAdd = new HashSet<>(); // roles to grant
	        Set<Role> rolesToDel = new HashSet<>(); // roles to delete
            
	        Set<String> spaces = roleService.getOrgSpaces(organization.getSlug());
	        Set<String> components = componentService.getConfigurations(organization.getId()).stream().map(c -> c.getComponentId()).collect(Collectors.toSet());
	        
	        for (RoleDTO dto: roles) {
	        	Role role = !StringUtils.isEmpty(dto.getSpace())
	        			? AACRoleDTO.concatRole(dto.getRole(), dto.getType(), organization.getSlug(), dto.getSpace())
	        			: AACRoleDTO.concatRole(dto.getRole(), dto.getType(), organization.getSlug());
	        	if (AACRoleDTO.isOrgRole(role)) continue;

	        	if (!StringUtils.isEmpty(dto.getSpace()) && !spaces.contains(dto.getSpace())) {
	        		throw new SecurityException("Unknown space: " + dto.getSpace());
	        	}
	        	if (AACRoleDTO.isComponentRole(role) && !components.contains(dto.getComponent())) {
	        		throw new SecurityException("Unknown component: " + dto.getComponent());
	        	}
	        	
	        	
	        	if (oldRoles.contains(role)) {
	        		oldRoles.remove(role);
	        	} else {
	        		rolesToAdd.add(role);
	        	}
	        }
	        
	        // removed roles
	        for (Role role : oldRoles) {
	        	rolesToDel.add(role);
	        }

	        // membership role is added for non-owners
	        if (!owner) rolesToAdd.add(AACRoleDTO.orgMember(organization.getSlug()));
	        
	        
	        Role ownerRole = AACRoleDTO.orgOwner(organization.getSlug());
	        boolean ownerBefore = allRoles.contains(ownerRole);
	        // removed from owners: remove from org and spaces
	        if (ownerBefore && !owner) {
	        	rolesToDel.add(ownerRole);
	        	for (String space: spaces) {
	        		rolesToDel.add(AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, Constants.ROOT_ORGANIZATIONS, organization.getSlug(), space));
	        	}
	        }
	        // Added to owners: add to org and spaces, and space resources
	        if (!ownerBefore && owner) {
	        	rolesToAdd.add(ownerRole);
	        	for (String space: spaces) {
	        		rolesToAdd.add(AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, Constants.ROOT_ORGANIZATIONS, organization.getSlug(), space));
	        	}
	        }
	        
            User toUpdate = new User();
            toUpdate.setUserId(userId);
            toUpdate.setUsername(userName);
            toUpdate.setRoles(rolesToDel);
            if (rolesToDel.size() > 0) {
                roleService.deleteRoles(Collections.singleton(toUpdate));
            }
            toUpdate.setRoles(rolesToAdd);
            if (rolesToAdd.size() > 0) {
                roleService.addRoles(Collections.singleton(toUpdate));
            }
            
            user.setRoles(roleService.getRoles(user, organization.getSlug()));
            return OrganizationMemberDTO.from(user);
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }

    /**
     * Removes a user from the organization.
     * 
     * @param organizationId - The organization the member will be removed from
     * @param memberId       - ID of the member to remove
     * @throws NoSuchUserException
     * @throws IdentityProviderAPIException
     * @throws NoSuchOrganizationException
     * @throws SystemException
     * @throws InvalidArgumentException
     */
    public void removeUser(long organizationId, String memberId)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException {

    	// finds the organization
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }
        // Checks if the user has permission to perform this action
        if (!OrgManagerUtils.userHasAdminRights() && !OrgManagerUtils.userIsOwner(organization.getSlug())) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.info("remove user " + memberId + " from organization " + String.valueOf(organizationId));

        try {
            // ID used by the identity provider for the
            // authenticated user
            String authenticatedId = OrgManagerUtils.getAuthenticatedUserId();
            if (!OrgManagerUtils.userHasAdminRights() && memberId.equals(authenticatedId)) {
                // non-admin are inside the organization so we can not remove them
                throw new InvalidArgumentException("You cannot remove yourself from the organization.");
            }

            User user = new User();
            user.setUserId(memberId);
            BasicProfile profile = profileService.getUserProfileById(memberId);
            if (profile == null) {
                logger.error("user " + memberId + " does not exists");
                throw new InvalidArgumentException("The user does not exists: " + memberId);
            }

            Set<Role> roles = roleService.getRoles(user, organization.getSlug());
            // update roles
            user.setRoles(roles);
            roleService.deleteRoles(Collections.singleton(user));
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }
}
