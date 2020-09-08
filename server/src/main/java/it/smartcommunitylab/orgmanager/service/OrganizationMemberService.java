package it.smartcommunitylab.orgmanager.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
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

        logger.debug("list users for organization " + organization);

        // orgs are listed in root context
        String context = Constants.ROOT_ORGANIZATIONS;

        // get members from role service
        return new ArrayList<>(roleService.getSpaceUsers(context, organization));

    }

    public OrganizationMemberDTO getUser(String organization, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        logger.info("get user " + userId + " from organization " + organization);

        BasicProfile profile = profileService.getUserProfileById(userId);
        Collection<AACRoleDTO> roles = roleService.getRoles(userId);

        // if user has no base role reject it
        String ownerMember = AACRoleDTO.ownerRole(Constants.ROOT_ORGANIZATIONS, organization).getAuthority();
        String orgMember = AACRoleDTO.memberRole(Constants.ROOT_ORGANIZATIONS, organization).getAuthority();

        if (roles.stream().anyMatch(r -> orgMember.equals(r.getAuthority()) || ownerMember.equals(r.getAuthority()))) {
            // filter users roles to expose only those related to org
            OrganizationMemberDTO member = OrganizationMemberDTO.from(
                    organization,
                    profile,
                    filterRoles(organization, roles, true));

            return member;
        } else {
            throw new NoSuchUserException(userId + " is not a member of " + organization);
        }
    }

    public OrganizationMemberDTO addUser(String organization, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        logger.info("add user " + userId + " to organization " + organization);
        BasicProfile profile = profileService.getUserProfileById(userId);
        Collection<AACRoleDTO> roles = roleService.getRoles(userId);

        AACRoleDTO orgMember = AACRoleDTO.memberRole(Constants.ROOT_ORGANIZATIONS, organization);

        if (!roles.contains(orgMember)) {
            roleService.addRoles(userId, Collections.singletonList(orgMember.getAuthority()));
        }

        return getUser(organization, userId);

    }

    public void removeUser(String organization, String userId)
            throws NoSuchUserException, SystemException, IdentityProviderAPIException {

        logger.info("remove user " + userId + " from organization " + organization);

        Collection<AACRoleDTO> roles = roleService.getRoles(userId);

        // filter roles and extract only those in org
        // fetch also roles in disabled components
        // ensure we do not remove org owner role otherwise org will disappear!
        List<AACRoleDTO> rolesToDel = filterRoles(organization, roles, false).stream()
                .filter(r -> (!(AACRoleDTO.isOrgRole(r, true) && r.getRole().equals(Constants.ROLE_OWNER))))
                .collect(Collectors.toList());

        // remove roles
        roleService.deleteRoles(userId,
                rolesToDel.stream().map(r -> r.getAuthority()).collect(Collectors.toList()));

    }

    public void addUserRoles(String organization, String userId, Collection<RoleDTO> roles)
            throws NoSuchUserException, SystemException, IdentityProviderAPIException {

        logger.info("add roles " + roles.toString() + " to user " + userId + " within organization " + organization);

        // TODO remove, no need to check
        BasicProfile profile = profileService.getUserProfileById(userId);
        if (profile == null) {
            logger.error("user " + userId + " does not exists");
            throw new NoSuchUserException();
        }

        // fetch current
        List<AACRoleDTO> curRoles = filterRoles(organization, roleService.getRoles(userId), false);
        Set<String> curAuthorities = curRoles.stream().map(r -> r.getAuthority()).collect(Collectors.toSet());

        Set<String> rolesToAdd = roles.stream()
                .map(d -> RoleDTO.to(organization, d))
                .map(r -> r.getAuthority())
                .filter(s -> !curAuthorities.contains(s))
                .collect(Collectors.toSet());

        if (!rolesToAdd.isEmpty()) {
            roleService.addRoles(userId, new ArrayList<>(rolesToAdd));
        }
    }

    public void removeUserRoles(String organization, String userId, Collection<RoleDTO> roles)
            throws NoSuchUserException, SystemException, IdentityProviderAPIException {

        logger.info(
                "remove roles " + roles.toString() + "from user " + userId + " within organization " + organization);

        // TODO remove, no need to check
        BasicProfile profile = profileService.getUserProfileById(userId);
        if (profile == null) {
            logger.error("user " + userId + " does not exists");
            throw new NoSuchUserException();
        }

        // fetch current
        List<AACRoleDTO> curRoles = filterRoles(organization, roleService.getRoles(userId), false);
        Set<String> curAuthorities = curRoles.stream().map(r -> r.getAuthority()).collect(Collectors.toSet());

        Set<String> rolesToDel = roles.stream()
                .filter(r -> (!Constants.ROLE_OWNER.equals(r.getRole()) && !Constants.ROLE_MEMBER.equals(r.getRole())))
                .map(d -> RoleDTO.to(organization, d))
                .map(r -> r.getAuthority())
                .filter(s -> curAuthorities.contains(s))
                .collect(Collectors.toSet());
        if (!rolesToDel.isEmpty()) {
            roleService.deleteRoles(userId, new ArrayList<>(rolesToDel));
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

        logger.info(
                "update roles for user " + userId + " from organization " + organization + " to " + roles.toString());

        try {
            // get user details
            // TODO remove, useless
            OrganizationMemberDTO member = getUser(organization, userId);
            logger.debug("old roles for user " + userId + ": " + member.getRoles().toString());

            // User is granted the roles listed in the request body.
            // previously granted roles not present in this new configuration will be
            // revoked.
            List<AACRoleDTO> newRoles = roles.stream().map(r -> RoleDTO.to(organization, r))
                    .collect(Collectors.toList());
            logger.debug("new roles for user " + userId + ": " + newRoles.toString());

            // re-read to fetch also roles in disabled components
            List<AACRoleDTO> oldRoles = filterRoles(organization, roleService.getRoles(userId), false);
            logger.debug("old roles for user " + userId + ": " + oldRoles.toString());

            // roles to grant
            Set<AACRoleDTO> candidateRolesToAdd = newRoles.stream()
                    .filter(r -> !oldRoles.contains(r))
                    .collect(Collectors.toSet());

            // roles to delete
            Set<AACRoleDTO> candidateRolesToDel = oldRoles.stream()
                    .filter(r -> !newRoles.contains(r))
                    .collect(Collectors.toSet());

            
            
            // ensure basic roles + roles definitions for components are NOT handled here
            Set<AACRoleDTO> rolesToAdd = candidateRolesToAdd.stream()
                    .filter(r -> (
                            !(Constants.ROLE_OWNER.equals(r.getRole())) &&                            
                            !(AACRoleDTO.isOrgRole(r, true) && Constants.ROLE_MEMBER.equals(r.getRole())) &&
                            !(AACRoleDTO.isOrgComponentRole(r))))
                    .collect(Collectors.toSet());
            Set<AACRoleDTO> rolesToDel = candidateRolesToDel.stream()
                    .filter(r -> (
                            !(Constants.ROLE_OWNER.equals(r.getRole())) &&                            
                            !(AACRoleDTO.isOrgRole(r, true) && Constants.ROLE_MEMBER.equals(r.getRole())) &&
                            !(AACRoleDTO.isOrgComponentRole(r))))
                    .collect(Collectors.toSet());

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
                logger.debug("roles toDel for user " + userId + ": " + rolesToDel.toString());
                List<String> rolesList = rolesToDel.stream()
                        .map(r -> r.getAuthority())
                        .collect(Collectors.toList());
                logger.debug("roles toDel list for user " + userId + ": " + rolesList.toString());

                roleService.deleteRoles(userId, rolesList);
            }
            if (rolesToAdd.size() > 0) {
                logger.debug("roles toAdd for user " + userId + ": " + rolesToAdd.toString());
                List<String> rolesList = rolesToAdd.stream()
                        .map(r -> r.getAuthority())
                        .collect(Collectors.toList());
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
        // 3. within org/components -> enabled, NOTE use this space for roles
        // definitions
        // 4. within components/space
        // 5. within org/resources
        // 6. within resources/spaces

        List<String> spaces = spaceService.listSpaces(organization);
        List<String> components = componentService.listComponents(organization);

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
                String componentId = AACRoleDTO.componentName(role);
                if (components.contains(componentId) || !enabled) {
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
