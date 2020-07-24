package it.smartcommunitylab.orgmanager.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//import java.util.regex.Pattern;

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
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.manager.ComponentManager;

@RestController
@Validated
public class ComponentController {

    @Autowired
    private ComponentManager componentManager;

//    private Pattern pattern = Pattern.compile(Constants.SLUG_PATTERN);

    /*
     * Configurations
     */
    @GetMapping("api/components")
    public List<ComponentDTO> listModels() {
        return componentManager.listModels();
    }

    @GetMapping("api/components/{componentId}/roles")
    public List<String> getComponentDefaultRoles(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchComponentException {
        return componentManager.getModel(componentId).getRoles();
    }

    /*
     * Organization components
     */

    @GetMapping("api/organizations/{slug}/components")
    public List<ComponentDTO> listComponents(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug)
            throws NoSuchOrganizationException, IdentityProviderAPIException {
        return componentManager.listComponents(slug);
    }

    @GetMapping("api/organizations/{slug}/components/{componentId}")
    public ComponentDTO getComponent(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchComponentException, IdentityProviderAPIException {
        return componentManager.getComponent(slug, componentId);
    }

//    @PostMapping("/api/organizations/{id}/components")
//    public List<ComponentConfigurationDTO> updateConfigurations(@PathVariable long id,
//            @RequestBody List<ComponentConfigurationDTO> configurationDTO)
//            throws NoSuchOrganizationException, SystemException, InvalidArgumentException {
//        return componentService.updateConfigurations(id, configurationDTO);
//    }
//    

    @PostMapping("api/organizations/{slug}/components")
    public List<ComponentDTO> addComponents(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestBody @Valid Collection<@Pattern(regexp = Constants.SLUG_PATTERN) String> componentIds)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {
        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

//        // validate and normalize space
//        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(Constants.SLUG_CHARS);

        List<ComponentDTO> components = new ArrayList<>();
        for (String componentId : componentIds) {
//            // normalize if needed
//            componentId = pattern.matcher(componentId).replaceAll(Constants.SLUG_FILL);

            components.add(componentManager.addComponent(slug, componentId, ownerId));
        }

        return components;
    }

    @PutMapping("api/organizations/{slug}/components")
    public ComponentDTO addComponent(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException, InvalidArgumentException {
        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

        return componentManager.addComponent(slug, componentId, ownerId);
    }

    @DeleteMapping("api/organizations/{slug}/components")
    public void deleteComponent(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestParam(required = false, defaultValue = "false") boolean cleanup)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException {

        componentManager.deleteComponent(slug, componentId, cleanup);
    }

    /*
     * Org component roles
     */
    @GetMapping("api/organizations/{slug}/components/{componentId}/roles")
    public List<String> getComponentRoles(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchComponentException {
        return componentManager.getComponent(slug, componentId).getRoles();
    }

    @PostMapping("api/organizations/{slug}/components/{componentId}/roles")
    public List<String> addComponentRoles(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestBody @Valid Collection<@Pattern(regexp = Constants.ROLE_PATTERN) String> roles)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {

        ComponentDTO component = componentManager.getComponent(slug, componentId);
        List<String> cRoles = component.getRoles();
        for (String role : roles) {
            cRoles = componentManager.addRole(slug, componentId, role);
        }

        return cRoles;
    }

    @PutMapping("api/organizations/{slug}/components/{componentId}/roles")
    public List<String> addComponentRole(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestParam @Valid @Pattern(regexp = Constants.ROLE_PATTERN) String role)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {

        return componentManager.addRole(slug, componentId, role);
    }

    @DeleteMapping("api/organizations/{slug}/components/{componentId}/roles")
    public List<String> deleteComponentRole(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestParam @Valid @Pattern(regexp = Constants.ROLE_PATTERN) String role)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException {

        return componentManager.deleteRole(slug, componentId, role);

    }

    /*
     * Org component spaces
     */

    @GetMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public List<String> getComponentSpaces(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchComponentException {
        return componentManager.listComponentSpaces(slug, componentId);
    }

    @PostMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public List<String> addComponentSpaces(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestBody @Valid Collection<@Pattern(regexp = Constants.SLUG_PATTERN) String> spaces)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException, NoSuchSpaceException {

        List<String> list = new ArrayList<>();
        for (String space : spaces) {
            // TODO check if space exists in org
            list.add(componentManager.registerComponentSpace(slug, componentId, space));
        }
        return list;
    }

    @PutMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public String addComponentSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String space)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException, NoSuchSpaceException {

        // TODO check if space exists in org
        return componentManager.registerComponentSpace(slug, componentId, space);
    }

    @DeleteMapping("api/organizations/{slug}/components/{componentId}/spaces")
    public void deleteComponentSpace(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String space,
            @RequestParam(required = false, defaultValue = "false") boolean cleanup)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchSpaceException {

        componentManager.unregisterComponentSpace(slug, componentId, space, cleanup);

    }

    /*
     * Helpers
     */
    // TODO replace with spring utils
//    private void validateSlug(String slug) throws InvalidArgumentException {
//        if (!pattern.matcher(slug).matches()) {
//            throw new InvalidArgumentException(
//                    "The string contains illegal characters (only lowercase alphanumeric characters and underscore are allowed): "
//                            + slug);
//        }
//    }
}
