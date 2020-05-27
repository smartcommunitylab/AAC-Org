package it.smartcommunitylab.orgmanager.manager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchComponentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.service.ComponentService;
import it.smartcommunitylab.orgmanager.service.OrganizationMemberService;
import it.smartcommunitylab.orgmanager.service.SpaceService;

@Service
public class MemberManager {
    private final static Logger logger = LoggerFactory.getLogger(MemberManager.class);

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private ComponentService componentService;

    @Autowired
    private OrganizationMemberService memberService;

    /*
     * Global
     */

    // TODO add pagination
    public List<OrganizationMemberDTO> listUsers() {
        // TODO ask roleService
        return Collections.emptyList();
    }

    /*
     * Org
     */

    public List<OrganizationMemberDTO> listUsers(String organization)
            throws SystemException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // return users with roles
        return memberService.getUsers(organization);

    }

    public OrganizationMemberDTO getUser(String organization, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return memberService.getUser(organization, userId);

    }

    public OrganizationMemberDTO addUser(String organization, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return memberService.addUser(organization, userId);
    }

    public void removeUser(String organization, String userId)
            throws NoSuchUserException, SystemException, InvalidArgumentException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // ID used by the identity provider for the
        // authenticated user
        String authenticatedId = OrgManagerUtils.getAuthenticatedUserId();
        if (!OrgManagerUtils.userHasAdminRights() && userId.equals(authenticatedId)) {
            // non-admin are inside the organization so we can not remove them
            throw new InvalidArgumentException("You cannot remove yourself from the organization.");
        }

        // remove user with roles
        memberService.removeUser(organization, userId);
    }

    /*
     * Roles
     */
    public OrganizationMemberDTO addUserRoles(String organization, String userId, Collection<RoleDTO> roles)
            throws NoSuchUserException, SystemException, InvalidArgumentException, IdentityProviderAPIException {

        // TODO extend permissions to let space/component providers to handle roles
        // within scope
        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // add user to org if possible
        try {
            OrganizationMemberDTO user = memberService.getUser(organization, userId);
        } catch (NoSuchUserException e) {
            // try to add as member
            memberService.addUser(organization, userId);
        }

        // add only valid roles compatible with org/space/components
        Set<RoleDTO> rolesToAdd = filterRoles(organization, roles);
        memberService.addUserRoles(organization, userId, rolesToAdd);

        return memberService.getUser(organization, userId);
    }

    public OrganizationMemberDTO removeUserRoles(String organization, String userId, Collection<RoleDTO> roles)
            throws NoSuchUserException, SystemException, InvalidArgumentException, IdentityProviderAPIException {

        // TODO extend permissions to let space/component providers to handle roles
        // within scope
        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // remove only valid roles compatible with org/space/components
        Set<RoleDTO> rolesToDel = filterRoles(organization, roles);
        memberService.removeUserRoles(organization, userId, rolesToDel);

        return memberService.getUser(organization, userId);
    }

    public OrganizationMemberDTO handleUserRoles(String organization, String userId, Collection<RoleDTO> roles,
            boolean provider)
            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException,
            IdentityProviderAPIException {

        // TODO extend permissions to let space/component providers to handle roles
        // within scope
        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // add user to org if possible
        try {
            OrganizationMemberDTO user = memberService.getUser(organization, userId);
        } catch (NoSuchUserException e) {
            // try to add as member
            memberService.addUser(organization, userId);
        }

        // keep only valid roles compatible with org/space/components
        Set<RoleDTO> newRoles = filterRoles(organization, roles);
        return memberService.handleUserRoles(organization, userId, newRoles, provider);
    }

    /*
     * Helpers
     */
    private Set<RoleDTO> filterRoles(String organization, Collection<RoleDTO> roles)
            throws IdentityProviderAPIException {
logger.trace("requested roles "+roles.toString());

        // fetch for org
        List<String> orgSpaces = spaceService.listSpaces(organization);
        List<String> orgComponents = componentService.listComponents(organization);

        // fetch roles spaces
        Set<String> roleSpaces = roles.stream()
                .filter(r -> StringUtils.hasText(r.getSpace())).map(r -> r.getSpace())
                .collect(Collectors.toSet());

        // fetch roles components
        Set<String> roleComponents = roles.stream()
                .filter(r -> StringUtils.hasText(r.getComponent())).map(r -> r.getComponent())
                .collect(Collectors.toSet());

        // fetch roles defined for each component in request
        Map<String, List<String>> componentRoles = new HashMap<>();
        for (String c : roleComponents) {
            if (orgComponents.contains(c)) {
                try {
                    ComponentDTO cd = componentService.getComponent(organization, c);
                    componentRoles.put(c, cd.getRoles());
                } catch (NoSuchComponentException e) {
                    // ignore
                }

            }
        }

        // fetch org spaces per component
        Map<String, List<String>> componentSpaces = new HashMap<>();
        for (String c : roleComponents) {
            if (orgComponents.contains(c)) {
                try {
                    // we need to decode spaces
                    List<String> cSpaces = componentService.listComponentSpaces(c);
                    List<String> coSpaces = orgSpaces.stream()
                            .filter(s -> cSpaces.contains(organization + Constants.SLUG_SEPARATOR + s))
                            .collect(Collectors.toList());

                    componentSpaces.put(c, coSpaces);

                } catch (NoSuchComponentException e) {
                    // ignore
                }

            }
        }
        
        logger.trace("component spaces "+componentSpaces.toString());
        logger.trace("component roles "+componentRoles.toString());

        // end-users can have roles in
        // 1. org
        // 2. spaces
        // 3. component spaces
        // 4. resources - TODO
        Set<RoleDTO> rolesToAdd = new HashSet<>();

        for (RoleDTO r : roles) {
            // ensure basic roles are excluded here
            if (Constants.ROLE_OWNER.equals(r.getRole()) || Constants.ROLE_MEMBER.equals(r.getRole())) {
                continue;
            }
            if (r.isOrgRole()) {
                rolesToAdd.add(r);
            } else if (r.isSpaceRole()) {
                // spaces need to exist in org
                if (orgSpaces.contains(r.getSpace())) {
                    // TODO define policy
                    rolesToAdd.add(r);
                }
            } else if (r.isComponentRole()) {
                // check if component is enabled for org
                // also check if role is defined for this component
                // also check if space is enabled for this component
                String componentId = r.getComponent();
                
                logger.trace("role check for "+r.getRole()+" space "+r.getSpace());
                
                if (componentRoles.containsKey(componentId) && componentSpaces.containsKey(componentId)) {
                    if (componentRoles.get(componentId).contains(r.getRole())
                            && componentSpaces.get(componentId).contains(r.getSpace())) {
                        rolesToAdd.add(r);
                    }
                }

            } else if (r.isResourceRole()) {
                // TODO define policy
                rolesToAdd.add(r);
            }

        }
        logger.trace("accepted roles "+rolesToAdd.toString());

        return rolesToAdd;
    }
}
