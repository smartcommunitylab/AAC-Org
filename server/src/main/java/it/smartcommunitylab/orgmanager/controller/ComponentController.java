package it.smartcommunitylab.orgmanager.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.InvalidArgumentException;
import it.smartcommunitylab.orgmanager.common.NoSuchComponentException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.common.SystemException;
import it.smartcommunitylab.orgmanager.dto.ComponentConfigurationDTO;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;
import it.smartcommunitylab.orgmanager.service.ComponentService;

@RestController
public class ComponentController {
    @Autowired
    private ComponentService componentService;

    /*
     * Configurations
     */
    @GetMapping("api/components")
    public List<ComponentDTO> listConfigurations() {
        return componentService.listConfigurations();
    }

    @GetMapping("api/components/{componentId}/roles")
    public List<String> getComponentDefaultRoles(@PathVariable String componentId) throws NoSuchComponentException {
        return componentService.getConfiguration(componentId).getRoles();
    }

    /*
     * Organization components
     */

    @GetMapping("api/organizations/{slug}/components")
    public List<ComponentDTO> getComponents(@PathVariable String slug)
            throws NoSuchOrganizationException, IdentityProviderAPIException {
        return componentService.getComponents(slug);
    }

//    @PostMapping("/api/organizations/{id}/components")
//    public List<ComponentConfigurationDTO> updateConfigurations(@PathVariable long id,
//            @RequestBody List<ComponentConfigurationDTO> configurationDTO)
//            throws NoSuchOrganizationException, SystemException, InvalidArgumentException {
//        return componentService.updateConfigurations(id, configurationDTO);
//    }
//    
    @PutMapping("api/organizations/{slug}/components")
    public ComponentDTO addComponent(@PathVariable String slug, @RequestParam String componentId)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {
        // set current user as owner
        String owner = OrgManagerUtils.getAuthenticatedUserName();

        return componentService.addComponent(slug, componentId, owner);
    }

    @DeleteMapping("api/organizations/{slug}/components")
    public void deleteComponent(@PathVariable String slug, @RequestParam String componentId)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException {

        // TODO delete all spaces

    }

    /*
     * Roles
     */
    @GetMapping("api/organizations/{slug}/components/{componentId}/roles")
    public List<String> getComponentRoles(@PathVariable String slug, @PathVariable String componentId)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchComponentException {
        return componentService.getComponent(slug, componentId).getRoles();
    }

    @PutMapping("api/organizations/{slug}/components/{componentId}/roles")
    public ComponentDTO addComponentRole(@PathVariable String slug, @PathVariable String componentId,
            @RequestParam String role)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {

        return componentService.addRole(slug, componentId, role);
    }

    @DeleteMapping("api/organizations/{slug}/components/{componentId}/roles")
    public void deleteComponentRole(@PathVariable String slug, @PathVariable String componentId,
            @RequestParam String role)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException {

        componentService.deleteRole(slug, componentId, role);

    }

    /*
     * Components
     */

    @GetMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public List<String> getComponentSpaces(@PathVariable String slug, @PathVariable String componentId)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchComponentException {
        // TODO filter spaces per org wrt defined
        return componentService.listComponentSpaces(componentId);
    }

    @PutMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public String addComponentSpace(@PathVariable String slug, @PathVariable String componentId,
            @RequestParam String space)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {

        // set current user as owner
        String owner = OrgManagerUtils.getAuthenticatedUserName();

        // TODO fetch providers
        List<String> providers = new ArrayList<>();
        return componentService.registerComponentSpace(slug, componentId, space, owner, providers);
    }

    @DeleteMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public void deleteComponentSpace(@PathVariable String slug, @PathVariable String componentId,
            @RequestParam String space)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchSpaceException {

        componentService.unregisterComponentSpace(slug, componentId, space);

    }

}
