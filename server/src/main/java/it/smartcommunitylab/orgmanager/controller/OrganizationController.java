package it.smartcommunitylab.orgmanager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.service.OrganizationService;

@RestController
public class OrganizationController {
	@Autowired
	private OrganizationService organizationService;
	
	@GetMapping("/api/organizations")
	public Page<OrganizationDTO> listOrganizations(String name, Pageable pageable) {
		return organizationService.listOrganizations(name, pageable);
	}
	
	@PostMapping("/api/organizations")
	public OrganizationDTO createOrganization(@RequestBody OrganizationDTO organizationDTO) {
		return organizationService.createOrganization(organizationDTO);
	}
	
	@PutMapping("/api/organizations/{id}/info")
	public OrganizationDTO updateOrganization(@PathVariable Long id, @RequestBody OrganizationDTO organizationDTO) {
		return organizationService.updateOrganization(id, organizationDTO);
	}
	
	@PutMapping("api/organizations/{id}/enable")
	public OrganizationDTO enableOrganization(@PathVariable Long id) {
		return organizationService.enableOrganization(id);
	}
	
	@PutMapping("api/organizations/{id}/disable")
	public OrganizationDTO disableOrganization(@PathVariable Long id) {
		return organizationService.disableOrganization(id);
	}
	
	@DeleteMapping("api/organizations/{id}")
	public void deleteOrganization(@PathVariable Long id) {
		organizationService.deleteOrganization(id);
	}
}
