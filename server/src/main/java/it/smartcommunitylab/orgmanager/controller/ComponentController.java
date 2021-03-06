package it.smartcommunitylab.orgmanager.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
//import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import it.smartcommunitylab.orgmanager.dto.ModelDTO;
import it.smartcommunitylab.orgmanager.dto.RoleDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;
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
    public List<ModelDTO> listModels() {
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
        return componentManager.listComponents(slug, true);
    }

    @GetMapping("api/organizations/{slug}/components/{componentId}")
    public ComponentDTO getComponent(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchComponentException, IdentityProviderAPIException {
        return componentManager.getComponent(slug, componentId, true);
    }

//    @PostMapping("/api/organizations/{id}/components")
//    public List<ComponentConfigurationDTO> updateConfigurations(@PathVariable long id,
//            @RequestBody List<ComponentConfigurationDTO> configurationDTO)
//            throws NoSuchOrganizationException, SystemException, InvalidArgumentException {
//        return componentService.updateConfigurations(id, configurationDTO);
//    }
//    

    @PostMapping("api/organizations/{slug}/components")
    public ComponentDTO addComponents(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @RequestBody @Valid ComponentDTO component)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException {
        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

        // extract data
        String componentId = component.getId();
        String name = component.getName();
        List<String> roles = component.getRoles();

        if (name != null) {
            // normalizes the name
            name = name.trim().replaceAll("\\s+", " ");
        }

        if (roles != null) {
            // validate
            // TODO
        }

        return componentManager.addComponent(slug, componentId, ownerId, roles);

    }

    @PutMapping("api/organizations/{slug}/components/{componentId}")
    public ComponentDTO createOrUpdateComponent(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestBody(required = false) ComponentDTO component)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
            NoSuchUserException, InvalidArgumentException, NoSuchSpaceException {

        // set current user as owner
        String ownerId = OrgManagerUtils.getAuthenticatedUserId();

        // extract data
        String name = component.getName();
        List<String> roles = component.getRoles();
        List<String> spaces = component.getSpaces().stream().map(s -> s.getId()).collect(Collectors.toList());

        if (name != null) {
            // normalizes the name
            name = name.trim().replaceAll("\\s+", " ");
        }

        if (roles != null) {
            // validate
            // TODO
        }

        ComponentDTO dto = null;

        try {
            dto = componentManager.updateComponent(slug, componentId, ownerId, name, roles);
        } catch (NoSuchComponentException noex) {
            // TODO save the name
            dto = componentManager.addComponent(slug, componentId, ownerId, roles);
        }

        if (spaces != null) {
            // validate
            // TODO

            // update
//            List<String> newSpaces = componentManager.updateComponentSpaces(slug, componentId, spaces);
//            dto.setSpaces(newSpaces.stream().map(s -> SpaceDTO.from(slug, s, s)).collect(Collectors.toList()));
            dto.setSpaces(componentManager.updateComponentSpaces(slug, componentId, spaces));
        }

        System.out.println("dump "+dto.getSpaces().toString());
        
        return dto;
    }

    @DeleteMapping("api/organizations/{slug}/components/{componentId}")
    public void deleteComponent(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
            @RequestParam(required = false, defaultValue = "true") boolean cleanup)
            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException {

        componentManager.deleteComponent(slug, componentId, cleanup);
    }

    /*
     * Org component roles
     */
    @GetMapping("api/organizations/{slug}/components/{componentId}/roles")
    public List<RoleDTO> getComponentRoles(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchComponentException {
        return componentManager.getComponent(slug, componentId, false).getRoles().stream()
                .map(r -> RoleDTO.from(RoleDTO.TYPE_COMPONENT, r, componentId, null))
                .collect(Collectors.toList());
    }

//    @PostMapping("api/organizations/{slug}/components/{componentId}/roles")
//    public List<String> addComponentRoles(
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
//            @RequestBody @Valid Collection<@Pattern(regexp = Constants.ROLE_PATTERN) String> roles)
//            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
//            NoSuchUserException {
//
//        ComponentDTO component = componentManager.getComponent(slug, componentId, false);
//        List<String> cRoles = component.getRoles();
//        for (String role : roles) {
//            cRoles = componentManager.addRole(slug, componentId, role);
//        }
//
//        return cRoles;
//    }
//
//    @PutMapping("api/organizations/{slug}/components/{componentId}/roles")
//    public List<String> addComponentRole(
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
//            @RequestParam @Valid @Pattern(regexp = Constants.ROLE_PATTERN) String role)
//            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
//            NoSuchUserException {
//
//        return componentManager.addRole(slug, componentId, role);
//    }

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
    public List<SpaceDTO> getComponentSpaces(
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId)
            throws NoSuchOrganizationException, IdentityProviderAPIException, NoSuchComponentException {
        return componentManager.listComponentSpaces(slug, componentId);
    }

//    @PostMapping("api/organizations/{slug}/components/{componentId}/spaces")
//    public List<String> addComponentSpaces(
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
//            @RequestBody @Valid Collection<@Pattern(regexp = Constants.SLUG_PATTERN) String> spaces)
//            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
//            NoSuchUserException, NoSuchSpaceException {
//
//        List<String> list = new ArrayList<>();
//        for (String space : spaces) {
//            // TODO check if space exists in org
//            list.add(componentManager.registerComponentSpace(slug, componentId, space));
//        }
//        return list;
//    }
//
//    @PutMapping("api/organizations/{slug}/components/{componentId}/spaces")
//    public String addComponentSpace(
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String slug,
//            @PathVariable @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String componentId,
//            @RequestParam @Valid @Pattern(regexp = Constants.SLUG_PATTERN) String space)
//            throws NoSuchOrganizationException, NoSuchComponentException, IdentityProviderAPIException,
//            NoSuchUserException, NoSuchSpaceException {
//
//        // TODO check if space exists in org
//        return componentManager.registerComponentSpace(slug, componentId, space);
//    }

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
