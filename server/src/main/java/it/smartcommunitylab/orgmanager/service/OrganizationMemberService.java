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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.Tenant;
import it.smartcommunitylab.orgmanager.repository.OrganizationRepository;
import it.smartcommunitylab.orgmanager.repository.TenantRepository;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationMemberService {
    private final static Logger logger = LoggerFactory.getLogger(OrganizationMemberService.class);

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

    @Autowired
    private ProfileService profileService;

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
            if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug())) {
                throw new AccessDeniedException(
                        "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
            }

            // get members from role service
            Set<User> users = roleService.getOrganizationMembers(organization.getSlug());

            // which purpose?
            // TODO remove
            String lcname = username.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getFullname().toLowerCase().contains(lcname))
                    .collect(Collectors.toSet());

            filterRoles(users, organization);
            return users.stream().map(u -> OrganizationMemberDTO.from(u)).collect(Collectors.toList());
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }

    private void filterRoles(Set<User> users, Organization organization) throws IdentityProviderAPIException {
        List<Tenant> tenants = tenantRepository.findByOrganization(organization);
        Set<String> orgTenantSpaces = tenants.stream()
                .map(t -> AACRoleDTO.tenantUser(t.getTenantId().getComponentId(), t.getTenantId().getName())
                        .canonicalSpace())
                .collect(Collectors.toSet());

        Set<String> orgSpaces = new HashSet<>(orgTenantSpaces);
        orgSpaces.add(AACRoleDTO.orgMember(organization.getSlug()).canonicalSpace());

        // TODO rework
        users.forEach(m -> {
            Set<Role> roles;
            try {
                roles = roleService.getRoles(m).stream().filter(r -> orgSpaces.contains(r.canonicalSpace()))
                        .collect(Collectors.toSet());
                m.setRoles(roles);

            } catch (IdentityProviderAPIException e) {
            }
        });
    }

    /**
     * User is granted the roles listed in the request body. Previously granted
     * roles not present in this new configuration will be revoked.
     * 
     * @param organizationId - ID of the organization to add the member to
     * @param memberDTO      - Request body, contains the member to add and their
     *                       roles
     * @return - The updated member
     * @throws SystemException
     * @throws NoSuchOrganizationException
     * @throws InvalidArgumentException
     * @throws NoSuchUserException
     */
    public OrganizationMemberDTO handleUserRoles(Long organizationId, String userName, Set<RoleDTO> roles)
            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException {

        // finds the organization
        Organization organization = organizationRepository.findById(organizationId).orElse(null);
        if (organization == null) {
            throw new NoSuchOrganizationException();
        }

        // Checks if the user has permission to perform this action
        if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug())) {
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

            List<Tenant> tenants = tenantRepository.findByOrganization(organization);
            // Builds a collection of all of the organization's tenants
            Set<String> orgTenants = new HashSet<String>();
            List<String> tenantNames = new ArrayList<String>();
            // List of just the names of the organization's tenants,
            // used for some connectors
            for (Tenant t : tenants) {
                orgTenants.add(Constants.ROOT_COMPONENTS + "/" + t.toString());
                tenantNames.add(t.getTenantId().getName());
            }

            // TODO rework logic and validate
            User user = new User();
            user.setUserId(userId);
            user.setUsername(userName);
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

            for (RoleDTO r : roles) {
                Role aacRole = AACRoleDTO.from(r);
                // cannot add roles for tenants non belonging to the organization
                if (AACRoleDTO.isComponentRole(aacRole) && !orgTenants.contains(aacRole.canonicalSpace())) {
                    throw new InvalidArgumentException(
                            "The following role is not within the organization's tenants: " + r);
                }
                if (AACRoleDTO.isComponentRole(aacRole)) {
                    String component = AACRoleDTO.componentName(aacRole);
                    // not changed, keep role in component
                    if (componentRolesToRemove.containsKey(component)
                            && componentRolesToRemove.get(component).contains(aacRole)) {
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
            }
            ;

            // check if owner of the organization
            if (organization.getOwner().equals(userName)) {
                rolesToAdd.add(AACRoleDTO.orgOwner(organization.getSlug()));
            } else {
                // set as member
                Role orgOwner = AACRoleDTO.orgOwner(organization.getSlug());
                rolesToAdd.remove(orgOwner);
                rolesToAdd.add(AACRoleDTO.orgMember(organization.getSlug()));
                rolesToDel.add(orgOwner);
            }

            // if any role is assigned, keep the org membership
            if (rolesToAdd.size() > 0) {
                rolesToDel.remove(AACRoleDTO.orgMember(organization.getSlug()));
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

            UserInfo userInfo = new UserInfo(profile.getUsername(), profile.getName(), profile.getSurname());
            // Handles user in the components
            Map<String, Component> componentMap = componentsModel.getListComponents();
            String resultMessage;
            for (String s : componentMap.keySet()) {
                // new roles to add
                if (componentRolesToAdd.containsKey(s)) {
                    // Create the user; components are supposed to not do anything if the user
                    // already exists
                    resultMessage = componentMap.get(s).createUser(userInfo);
                    if (CommonUtils.isErroneousResult(resultMessage))
                        throw new EntityNotFoundException(resultMessage);

                    Set<Role> added = componentRolesToAdd.get(s);
                    for (Role r : added) {
                        resultMessage = componentMap.get(s).assignRoleToUser(r.getSpace() + ":" + r.getRole(),
                                organization.getName(), userInfo);
                        if (CommonUtils.isErroneousResult(resultMessage))
                            throw new EntityNotFoundException(resultMessage);
                    }
                    // remove user as no roles exist
                } else if (!componentRolesToKeep.containsKey(s)) {
                    resultMessage = componentMap.get(s).removeUserFromOrganization(userInfo, organization.getName(),
                            tenantNames);
                    if (CommonUtils.isErroneousResult(resultMessage))
                        throw new EntityNotFoundException(resultMessage);
                    // remove roles that are not used
                } else {
                    Set<Role> removed = componentRolesToRemove.get(s);
                    for (Role r : removed) {
                        resultMessage = componentMap.get(s).revokeRoleFromUser(r.getSpace() + ":" + r.getRole(),
                                organization.getName(), userInfo);
                        if (CommonUtils.isErroneousResult(resultMessage))
                            throw new EntityNotFoundException(resultMessage);
                    }

                }
            }

            // TODO check
            user.setRoles(roleService.getRoles(user));
            filterRoles(Collections.singleton(user), organization);

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
        if (!utils.userHasAdminRights() && !utils.userIsOwner(organization.getSlug())) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.info("remove user " + memberId + " from organization " + String.valueOf(organizationId));

        try {
            // ID used by the identity provider for the
            // authenticated user
            String authenticatedId = utils.getAuthenticatedUserId();
            if (!utils.userHasAdminRights() && memberId.equals(authenticatedId)) {
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

            UserInfo userInfo = new UserInfo(profile.getUsername(), profile.getName(), profile.getSurname());
            Set<Role> roles = roleService.getRoles(user);
            List<Tenant> tenants = tenantRepository.findByOrganization(organization);
            List<String> tenantNames = new LinkedList<>();

            // List of just the names of the organization's tenants, used
            // for some connectors
            Set<String> orgSpaces = new HashSet<String>();
            for (Tenant t : tenants) {
                orgSpaces.add(Constants.ROOT_COMPONENTS + "/" + t.toString());
                tenantNames.add(t.getTenantId().getName());
            }
            orgSpaces.add(AACRoleDTO.orgMember(organization.getSlug()).canonicalSpace());

            // filter roles for member
            roles = roles.stream().filter(r -> orgSpaces.contains(r.canonicalSpace())).collect(Collectors.toSet());

            // update roles
            user.setRoles(roles);
            roleService.deleteRoles(Collections.singleton(user));

            // Removes the user for the components
            Map<String, Component> componentMap = componentsModel.getListComponents();
            for (String s : componentMap.keySet()) {
                String resultMessage = componentMap.get(s).removeUserFromOrganization(userInfo, organization.getName(),
                        tenantNames);
                if (CommonUtils.isErroneousResult(resultMessage)) {
                    // do we care?
                    // TODO evaluate
                    throw new EntityNotFoundException(resultMessage);
                }
            }
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }
}
