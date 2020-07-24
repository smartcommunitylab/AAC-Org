package it.smartcommunitylab.orgmanager.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.manager.MemberManager;

@RestController
@Validated
public class MemberController {

    @Autowired
    private MemberManager memberManager;

    /*
     * Global
     */
    @GetMapping("/api/users")
    public List<OrganizationMemberDTO> listUsers()
            throws SystemException, IdentityProviderAPIException {
        return memberManager.listUsers();
    }

    /*
     * Org
     */
    @GetMapping("/api/organizations/{slug}/members")
    public List<OrganizationMemberDTO> listMembers(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug)
            throws SystemException, IdentityProviderAPIException {
        return memberManager.listUsers(slug);
    }

    @PostMapping("api/organizations/{slug}/members")
    public List<OrganizationMemberDTO> addMembers(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestBody @Valid @Pattern(regexp = Constants.USERID_PATTERN) String[] members)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException,
            IdentityProviderAPIException {

        List<OrganizationMemberDTO> list = new ArrayList<>();
        for (String userId : members) {
            list.add(memberManager.addUser(slug, userId));
        }

        return list;

    }

    @GetMapping("/api/organizations/{slug}/members/{memberId}")
    public OrganizationMemberDTO getMember(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.USERID_PATTERN) String memberId)
            throws SystemException, IdentityProviderAPIException, NoSuchUserException {
        return memberManager.getUser(slug, memberId);
    }

    @DeleteMapping("api/organizations/{slug}/members/{memberId}")
    public void removeMember(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.USERID_PATTERN) String memberId)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException,
            IdentityProviderAPIException {
        memberManager.removeUser(slug, memberId);
    }

    /*
     * Roles
     */

//    @PostMapping("/api/organizations/{slug}/members")
//    public OrganizationMemberDTO handleMemberRoles(
//            @Valid @Pattern(regexp = Constants.SLUG_PATTERN) @PathVariable String slug,
//            @RequestBody OrganizationMemberDTO memberDTO)
//            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException,
//            IdentityProviderAPIException {
//
//        return memberManager.handleUserRoles(slug, memberDTO.getId(), memberDTO.getRoles(), false);
//    }

    @PutMapping("api/organizations/{slug}/members/{memberId}")
    public OrganizationMemberDTO handleMemberRoles(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.USERID_PATTERN) String memberId,
            @RequestBody RoleDTO[] roles)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException,
            IdentityProviderAPIException {

        // replace all user roles within org with these
        List<RoleDTO> newRoles = Arrays.asList(roles);
        return memberManager.handleUserRoles(slug, memberId, newRoles, false);
    }

    @PostMapping("api/organizations/{slug}/members/{memberId}")
    public OrganizationMemberDTO addMemberRoles(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.USERID_PATTERN) String memberId,
            @RequestBody(required = false) RoleDTO[] roles)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException,
            IdentityProviderAPIException {
        if (roles == null) {
            // add user as member
            return memberManager.addUser(slug, memberId);
        } else {
            // add new roles to user
            List<RoleDTO> newRoles = Arrays.asList(roles);
            return memberManager.addUserRoles(slug, memberId, newRoles);
        }
    }

}
