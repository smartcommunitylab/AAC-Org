package it.smartcommunitylab.orgmanager.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.dto.UserRightsDTO;
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

    @GetMapping("/api/organizations/{id}/members")
    public List<OrganizationMemberDTO> getUsers(@PathVariable long id, String username) throws SystemException {
        return organizationMemberService.getUsers(id, username);
    }

    @GetMapping("/api/auths")
    public UserRightsDTO getUserRights() {
        return OrgManagerUtils.getUserRights();
    }

    @PostMapping("/api/organizations/{id}/members")
    public OrganizationMemberDTO handleUserRoles(@PathVariable long id, @RequestBody OrganizationMemberDTO memberDTO)
            throws SystemException, NoSuchOrganizationException, InvalidArgumentException, NoSuchUserException {
        // extract
        String userName = memberDTO.getUsername();
        Set<RoleDTO> roles = memberDTO.getRoles();
        return organizationMemberService.handleUserRoles(id, userName, roles);
    }

    @DeleteMapping("api/organizations/{organizationId}/members/{memberId}")
    public void removeUserRoles(@PathVariable long organizationId, @PathVariable String memberId)
            throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException {
        organizationMemberService.removeUser(organizationId, memberId);
    }
}
