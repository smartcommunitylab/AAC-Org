package it.smartcommunitylab.orgmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.dto.OrganizationMemberDTO;
import it.smartcommunitylab.orgmanager.service.OrganizationMemberService;

@RestController
public class OrganizationMemberController {
	@Autowired
	private OrganizationMemberService organizationMemberService;
	
	@GetMapping("/api/organizations/{id}/members")
	public List<OrganizationMemberDTO> getUsers(@PathVariable Long id, String username) {
		return organizationMemberService.getUsers(id, username);
	}
	
	@PostMapping("/api/organizations/{id}/members")
	public OrganizationMemberDTO handleUserRoles(@PathVariable Long id, @RequestBody OrganizationMemberDTO memberDTO) {
		return organizationMemberService.handleUserRoles(id, memberDTO);
	}
	
	@DeleteMapping("api/organizations/{organizationId}/members/{memberId}")
	public void removeUserRoles(@PathVariable Long organizationId, @PathVariable Long memberId) {
		organizationMemberService.removeUser(organizationId, memberId);
	}
	
	@PostMapping("/api/organizations/{id}/owners")
	public OrganizationMemberDTO addOwner(@PathVariable Long id, @RequestBody OrganizationMemberDTO memberDTO) {
		return organizationMemberService.addOwner(id, memberDTO);
	}
	
	@DeleteMapping("/api/organizations/{organizationId}/owners/{ownerId}")
	public void removeOwner(@PathVariable Long organizationId, @PathVariable Long ownerId) {
		organizationMemberService.removeOwner(organizationId, ownerId);
	}
}
