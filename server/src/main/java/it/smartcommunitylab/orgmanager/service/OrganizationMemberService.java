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

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
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
@Transactional(rollbackFor = Exception.class)
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
     * @param username       - If specified, only users whose name contains this
     *                       string will be returned
     * @return - Users in the organization
     * @throws IdentityProviderAPIException
     */
    public List<OrganizationMemberDTO> getUsers(long organizationId, String username)
            throws SystemException {
        if (username == null) {
            // null is not accepted, but an empty string works just fine when a filter on
            // the name is not desired
            username = "";
        }

        logger.debug("list users for organization " + String.valueOf(organizationId) + " matching name: " + username);

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
	        Set<Role> rolesToDel = new HashSet<>(oldRoles); // roles to delete
            
	        Set<String> spaces = roleService.getOrgSpaces(organization.getSlug());
	        Set<String> components = componentService.getConfigurations(organization.getId()).stream().map(c -> c.getComponentId()).collect(Collectors.toSet());
	        
	        for (RoleDTO dto: roles) {
	        	if (!spaces.contains(dto.getSpace())) {
	        		throw new SecurityException("Unknown space: " + dto.getSpace());
	        	}
	        	if (!components.contains(dto.getComponent())) {
	        		throw new SecurityException("Unknown component: " + dto.getComponent());
	        	}
	        	Role role = AACRoleDTO.concatRole(dto.getRole(), dto.getType(), organization.getSlug(), dto.getSpace());
	        	if (AACRoleDTO.isOrgRole(role)) continue;
	        	
	        	
	        	if (oldRoles.contains(role)) {
	        		oldRoles.remove(role);
	        	} else {
	        		rolesToAdd.add(role);
	        	}
	        }
	        
	        for (Role role : oldRoles) {
	        	rolesToDel.add(role);
	        }

	        // TODO compare owner before and after:
	        // - if added, add to org and spaces
	        // - if removed, remove from org and spaces
	        // TODO if has any role, add membership role
	        
//            Set<Role> oldRoles = roleService.getRoles(user);
//            Set<Role> rolesToAdd = new HashSet<>(); // roles to grant
//            Set<Role> rolesToDel = new HashSet<>(oldRoles); // roles to delete
//            Map<String, Set<Role>> componentRolesToAdd = new HashMap<>();
//            Map<String, Set<Role>> componentRolesToRemove = new HashMap<>();
//            Map<String, Set<Role>> componentRolesToKeep = new HashMap<>();
//            oldRoles.forEach(r -> {
//                if (AACRoleDTO.isComponentRole(r) && orgTenants.contains(r.canonicalSpace())) {
//                    String component = AACRoleDTO.componentName(r);
//                    Set<Role> set = componentRolesToRemove.getOrDefault(component, new HashSet<>());
//                    set.add(r);
//                    componentRolesToRemove.put(component, set);
//                } else {
//                    rolesToDel.remove(r);
//                }
//            });
//
//            for (RoleDTO r : roles) {
//                Role aacRole = AACRoleDTO.from(r);
//                // cannot add roles for tenants non belonging to the organization
//                if (AACRoleDTO.isComponentRole(aacRole) && !orgTenants.contains(aacRole.canonicalSpace())) {
//                    throw new InvalidArgumentException(
//                            "The following role is not within the organization's tenants: " + r);
//                }
//                if (AACRoleDTO.isComponentRole(aacRole)) {
//                    String component = AACRoleDTO.componentName(aacRole);
//                    // not changed, keep role in component
//                    if (componentRolesToRemove.containsKey(component)
//                            && componentRolesToRemove.get(component).contains(aacRole)) {
//                        componentRolesToRemove.get(component).remove(aacRole);
//                        Set<Role> set = componentRolesToKeep.getOrDefault(component, new HashSet<>());
//                        set.add(aacRole);
//                        componentRolesToKeep.put(component, set);
//                    } else {
//                        Set<Role> set = componentRolesToAdd.getOrDefault(component, new HashSet<>());
//                        set.add(aacRole);
//                        componentRolesToAdd.put(component, set);
//                    }
//                    rolesToDel.remove(aacRole);
//                    rolesToAdd.add(aacRole);
//                }
//            }
//            ;
//
//            // check if owner of the organization
//            if (organization.getOwner().equals(userName)) {
//                rolesToAdd.add(AACRoleDTO.orgOwner(organization.getSlug()));
//            } else {
//                // set as member
//                Role orgOwner = AACRoleDTO.orgOwner(organization.getSlug());
//                rolesToAdd.remove(orgOwner);
//                rolesToAdd.add(AACRoleDTO.orgMember(organization.getSlug()));
//                rolesToDel.add(orgOwner);
//            }
//
//            // if any role is assigned, keep the org membership
//            if (rolesToAdd.size() > 0) {
//                rolesToDel.remove(AACRoleDTO.orgMember(organization.getSlug()));
//            }
//
//            User toUpdate = new User();
//            toUpdate.setUserId(userId);
//            toUpdate.setUsername(userName);
//            toUpdate.setRoles(rolesToDel);
//            if (rolesToDel.size() > 0) {
//                roleService.deleteRoles(Collections.singleton(toUpdate));
//            }
//            toUpdate.setRoles(rolesToAdd);
//            if (rolesToAdd.size() > 0) {
//                roleService.addRoles(Collections.singleton(toUpdate));
//            }
//
//            // TODO check
//            user.setRoles(roleService.getRoles(user));
//            filterRoles(Collections.singleton(user), organization);

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
