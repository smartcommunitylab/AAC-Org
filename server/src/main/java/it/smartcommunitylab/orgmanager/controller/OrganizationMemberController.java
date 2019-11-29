package it.smartcommunitylab.orgmanager.controller;

import java.util.List;

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
import it.smartcommunitylab.orgmanager.service.OrganizationMemberService;

@RestController
public class OrganizationMemberController {

    @Autowired
    private OrganizationMemberService organizationMemberService;

    @Autowired
    private OrgManagerUtils utils;

    @GetMapping("/api/organizations/{id}/members")
    public List<OrganizationMemberDTO> getUsers(@PathVariable long id, String username) {
        return organizationMemberService.getUsers(id, username);
    }

    @GetMapping("/api/auths")
    public UserRightsDTO getUserRights() {
        return organizationMemberService.getUserRights();
    }

    @PostMapping("/api/organizations/{id}/members")
    public OrganizationMemberDTO handleUserRoles(@PathVariable long id, @RequestBody OrganizationMemberDTO memberDTO) {
        return organizationMemberService.handleUserRoles(id, memberDTO);
    }

    @DeleteMapping("api/organizations/{organizationId}/members/{memberId}")
    public void removeUserRoles(@PathVariable long organizationId, @PathVariable String memberId) throws NoSuchUserException, NoSuchOrganizationException, SystemException, InvalidArgumentException {
        organizationMemberService.removeUser(organizationId, memberId);
    }
}
