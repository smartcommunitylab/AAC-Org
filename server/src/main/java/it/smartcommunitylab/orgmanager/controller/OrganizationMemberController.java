package it.smartcommunitylab.orgmanager.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.dto.UserRightsDTO;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.service.OrganizationMemberService;

@RestController
public class OrganizationMemberController {

    @Autowired
    private OrganizationMemberService organizationMemberService;

    @GetMapping("/api/organizations/{slug}/members")
    public List<OrganizationMemberDTO> getUsers(@PathVariable String slug)
            throws SystemException, IdentityProviderAPIException {
        return organizationMemberService.getUsers(slug);
    }

    @PutMapping("api/organizations/{slug}/members/{memberId}")
    public OrganizationMemberDTO addUser(@PathVariable String slug, @PathVariable String memberId)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException,
            IdentityProviderAPIException {
        return organizationMemberService.addUser(slug, memberId);
    }

    @DeleteMapping("api/organizations/{slug}/members/{memberId}")
    public void removeUser(@PathVariable String slug, @PathVariable String memberId)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException {
        organizationMemberService.removeUser(slug, memberId);
    }

    @PostMapping("/api/organizations/{slug}/members")
    public OrganizationMemberDTO handleUserRoles(@PathVariable String slug,
            @RequestBody OrganizationMemberDTO memberDTO)
            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException {
        // extract
        String userName = memberDTO.getUsername();
        Set<RoleDTO> roles = memberDTO.getRoles();
        return organizationMemberService.handleUserRoles(slug, userName, roles, memberDTO.getOwner());
    }

}
