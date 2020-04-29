package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collection;
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

@Service
public class OrganizationMemberService {
    private final static Logger logger = LoggerFactory.getLogger(OrganizationMemberService.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private ComponentService componentService;
//
//    @Autowired
//    private ComponentService componentService;

    /**
     * Lists users within an organization.
     * 
     * @param organizationId - ID of the organization
     * @return - Users in the organization
     * @throws IdentityProviderAPIException
     */
    public List<OrganizationMemberDTO> getUsers(String organization)
            throws SystemException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        logger.debug("get users for organization " + organization);

        // get members
        List<String> users = listUsers(organization);

        List<OrganizationMemberDTO> members = new ArrayList<>();

        // fetch for each user all the roles within organization spaces
        for (String userId : users) {
            try {
                members.add(getUser(organization, userId));
            } catch (NoSuchUserException e) {
                logger.error("User " + userId + " not valid, skip");
            }
        }

        return members;

    }

    public List<String> listUsers(String organization)
            throws SystemException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.debug("list users for organization " + organization);

        // orgs are listed in root context
        String context = Constants.ROOT_ORGANIZATIONS;

        // get members from role service
        return new ArrayList<>(roleService.getSpaceUsers(context, organization));

    }

    public OrganizationMemberDTO getUser(String organization, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.info("get user " + userId + " from organization " + organization);

        BasicProfile profile = profileService.getUserProfileById(userId);
        Collection<AACRoleDTO> roles = roleService.getRoles(userId);

        // filter users roles to expose only those related to org
        OrganizationMemberDTO member = OrganizationMemberDTO.from(
                profile,
                filterRoles(organization, roles, true));

        return member;

    }

    public OrganizationMemberDTO addUser(String organization, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.info("add user " + userId + " to organization " + organization);

        Role orgMember = AACRoleDTO.memberRole(AACRoleDTO.ORGANIZATION_PREFIX, organization);

        roleService.addRoles(userId, Collections.singletonList(orgMember.getAuthority()));

        return getUser(organization, userId);

    }

    public void removeUser(String organization, String userId)
            throws NoSuchUserException, SystemException, InvalidArgumentException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.info("remove user " + userId + " from organization " + organization);

        try {
            // ID used by the identity provider for the
            // authenticated user
            String authenticatedId = OrgManagerUtils.getAuthenticatedUserId();
            if (!OrgManagerUtils.userHasAdminRights() && userId.equals(authenticatedId)) {
                // non-admin are inside the organization so we can not remove them
                throw new InvalidArgumentException("You cannot remove yourself from the organization.");
            }

            // TODO remove, no need to check
            BasicProfile profile = profileService.getUserProfileById(userId);
            if (profile == null) {
                logger.error("user " + userId + " does not exists");
                throw new InvalidArgumentException("The user does not exists: " + userId);
            }

            Collection<AACRoleDTO> roles = roleService.getRoles(userId);

            // filter roles and extract only those in org
            // fetch also roles in disabled components
            List<AACRoleDTO> rolesToDel = filterRoles(organization, roles, false);

            // remove roles
            roleService.deleteRoles(userId,
                    rolesToDel.stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
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
    public OrganizationMemberDTO handleUserRoles(String organization, String userId, Set<RoleDTO> roles,
            boolean provider)
            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException(
                    "Access is denied: user is not registered as owner of the organization and does not have administrator rights.");
        }

        logger.info("update roles for user " + userId + " from organization " + organization);

        try {
            // get user details
            OrganizationMemberDTO member = getUser(organization, userId);
            logger.debug("old roles for user " + userId + ": " + member.getRoles().toString());

            // User is granted the roles listed in the request body.
            // previously granted roles not present in this new configuration will be
            // revoked.
            List<AACRoleDTO> newRoles = roles.stream().map(r -> RoleDTO.to(organization, r))
                    .collect(Collectors.toList());

            // re-read to fetch also roles in disabled components
            List<AACRoleDTO> oldRoles = filterRoles(organization, roleService.getRoles(userId), false);
            Set<AACRoleDTO> rolesToAdd = new HashSet<>(); // roles to grant
            Set<AACRoleDTO> rolesToDel = new HashSet<>(); // roles to delete

            // first pass, add removed roles
            for (AACRoleDTO r : oldRoles) {
                if (!newRoles.contains(r)) {
                    rolesToDel.add(r);
                }
            }

            // second pass, add new roles
            for (AACRoleDTO r : newRoles) {
                if (!oldRoles.contains(r)) {
                    rolesToAdd.add(r);
                }
            }

            // also check if provider role requested
            AACRoleDTO providerRole = AACRoleDTO.providerRole(Constants.ROOT_ORGANIZATIONS, organization);
            if (provider) {
                if (!oldRoles.contains(providerRole)) {
                    rolesToAdd.add(providerRole);
                }
                // ensure it's not accidentally removed
                rolesToDel.remove(providerRole);

            } else {
                if (oldRoles.contains(providerRole)) {
                    rolesToDel.add(providerRole);
                }
                // ensure it's not accidentally added
                rolesToAdd.remove(providerRole);
            }

            // TODO handle provider for org spaces
            //

            // update roles
            if (rolesToDel.size() > 0) {
                List<String> rolesList = rolesToDel.stream().map(r -> r.getAuthority()).collect(Collectors.toList());
                roleService.deleteRoles(userId, rolesList);
            }
            if (rolesToAdd.size() > 0) {
                List<String> rolesList = rolesToAdd.stream().map(r -> r.getAuthority()).collect(Collectors.toList());
                roleService.addRoles(userId, rolesList);
            }

            // fetch again full roles set
            return getUser(organization, userId);

            // DEPRECATED logic
//            BasicProfile profile = profileService.getUserProfile(userName);
//            if (profile == null) {
//                logger.error("user " + userName + " does not exists");
//                throw new InvalidArgumentException("The user does not exists: " + userName);
//            }
//
//            String userId = profile.getUserId();
//
//            User user = new User();
//            user.setUserId(userId);
//            user.setUsername(userName);
//
//            Set<Role> allRoles = roleService.getRoles(user, slug);
//
//            Set<Role> oldRoles = allRoles.stream().filter(r -> !AACRoleDTO.isOrgRole(r)).collect(Collectors.toSet());
//            Set<Role> rolesToAdd = new HashSet<>(); // roles to grant
//            Set<Role> rolesToDel = new HashSet<>(); // roles to delete
//
//            Set<String> spaces = roleService.getOrgSpaces(slug);
////            Set<String> components = componentService.getConfigurations(slug).stream().map(c -> c.getComponentId())
////                    .collect(Collectors.toSet());
//            Set<String> components = Collections.emptySet();
//
//            for (RoleDTO dto : roles) {
//                Role role = !StringUtils.isEmpty(dto.getSpace())
//                        ? AACRoleDTO.concatRole(dto.getRole(), dto.getType(), slug, dto.getSpace())
//                        : AACRoleDTO.concatRole(dto.getRole(), dto.getType(), slug);
//                if (AACRoleDTO.isOrgRole(role))
//                    continue;
//
//                if (!StringUtils.isEmpty(dto.getSpace()) && !spaces.contains(dto.getSpace())) {
//                    throw new SecurityException("Unknown space: " + dto.getSpace());
//                }
//                if (AACRoleDTO.isComponentRole(role) && !components.contains(dto.getComponent())) {
//                    throw new SecurityException("Unknown component: " + dto.getComponent());
//                }
//
//                if (oldRoles.contains(role)) {
//                    oldRoles.remove(role);
//                } else {
//                    rolesToAdd.add(role);
//                }
//            }
//
//            // removed roles
//            for (Role role : oldRoles) {
//                rolesToDel.add(role);
//            }
//
//            // membership role is added for non-owners
//            if (!owner)
//                rolesToAdd.add(AACRoleDTO.orgMember(slug));
//
//            Role ownerRole = AACRoleDTO.orgOwner(slug);
//            boolean ownerBefore = allRoles.contains(ownerRole);
//            // removed from owners: remove from org and spaces
//            if (ownerBefore && !owner) {
//                rolesToDel.add(ownerRole);
//                for (String space : spaces) {
//                    rolesToDel.add(
//                            AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, Constants.ROOT_ORGANIZATIONS, slug, space));
//                }
//            }
//            // Added to owners: add to org and spaces, and space resources
//            if (!ownerBefore && owner) {
//                rolesToAdd.add(ownerRole);
//                for (String space : spaces) {
//                    rolesToAdd.add(
//                            AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, Constants.ROOT_ORGANIZATIONS, slug, space));
//                }
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
//            user.setRoles(roleService.getRoles(user, slug));
//            return OrganizationMemberDTO.from(user);
        } catch (IdentityProviderAPIException e) {
            logger.error(e.getMessage());
            throw new SystemException(e.getMessage(), e);
        }
    }

    /*
     * Helpers
     */

    private List<AACRoleDTO> filterRoles(String organization, Collection<AACRoleDTO> roles, boolean enabled)
            throws IdentityProviderAPIException {

        // TODO fetch all prefixes
        // roles are
        // 1. within organization
        // 2. within org/spaces
        // 3. within org/components
        // 4. within components/space
        // 5. within org/resources
        // 6. within resources/spaces

        List<String> spaces = spaceService.listSpaces(organization).stream().map(s -> s.getSlug())
                .collect(Collectors.toList());
        List<String> components = componentService.listComponents(organization).stream().map(s -> s.getSlug())
                .collect(Collectors.toList());

        // filter
        List<AACRoleDTO> orgRoles = new ArrayList<>();
        for (AACRoleDTO role : roles) {
            if (AACRoleDTO.isOrgRole(role, false)) {
                // 1. should match org as space
                if (role.getContext().equals(Constants.ROOT_ORGANIZATIONS) && organization.equals(role.getSpace())) {
                    orgRoles.add(role);
                }

                // 2. also add roles in subspaces
                if (role.getContext().equals(spaceService.getOrgContext(organization))) {
                    orgRoles.add(role);
                }

                // 3. also add roles at org component level
                if (role.getContext().equals(componentService.getOrgContext(organization))) {
                    orgRoles.add(role);
                }

                // TODO 5.
            }

            if (AACRoleDTO.isComponentRole(role)) {
                // 3. check if component is enabled in org
                if (components.contains(AACRoleDTO.componentName(role)) || !enabled) {
                    // 4. check if space belongs to this org
                    // NOTE: when flattened, org spaces slug are composed as [org]-[space]
                    // TODO rework to avoid loop
                    for (String space : spaces) {
                        String cSpace = organization + Constants.SLUG_SEPARATOR + space;
                        if (cSpace.equals(role.getSpace())) {
                            orgRoles.add(role);
                            break;
                        }
                    }
                }
            }

            if (AACRoleDTO.isResourceRole(role)) {
                // TODO 6.
            }

        }

        return orgRoles;
    }
}
