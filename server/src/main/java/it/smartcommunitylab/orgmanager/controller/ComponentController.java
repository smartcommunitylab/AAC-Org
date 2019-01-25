package it.smartcommunitylab.orgmanager.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.dto.ComponentConfigurationDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.service.ComponentService;

@RestController
public class ComponentController {
	@Autowired
	private ComponentService componentService;
	
	@GetMapping("/api/components")
	public Page<ComponentDTO> listComponents(Pageable pageable) {
		return componentService.listComponents(pageable);
	}
	
	@PostMapping("/api/organizations/{id}/configuration")
	public List<ComponentConfigurationDTO> updateConfigurations(@PathVariable Long id, @RequestBody List<ComponentConfigurationDTO> configurationDTO) {
		return componentService.updateConfigurations(id, configurationDTO);
	}
}
