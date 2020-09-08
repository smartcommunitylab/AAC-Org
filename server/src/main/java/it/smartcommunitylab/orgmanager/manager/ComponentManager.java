package it.smartcommunitylab.orgmanager.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchComponentException;
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.dto.ComponentDTO;
import it.smartcommunitylab.orgmanager.dto.ModelDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;
import it.smartcommunitylab.orgmanager.service.ComponentService;
import it.smartcommunitylab.orgmanager.service.SpaceService;

@Service
public class ComponentManager {

    @Autowired
    private ComponentService componentService;

    @Autowired
    private SpaceService spaceService;

    /*
     * Models
     */
    public List<ModelDTO> listModels() {
        return componentService.listModels();
    }

    public ModelDTO getModel(String component) throws NoSuchComponentException {
        return componentService.getModel(component);
    }

    /*
     * Org components
     */

    public List<ComponentDTO> listComponents(String organization, boolean inflate)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org components
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        List<ComponentDTO> components = componentService.getComponents(organization);
        if (inflate) {
            for (ComponentDTO c : components) {
                // enrich component with spaces info
                try {
                    c.setSpaces(listComponentSpaces(organization, c.getId()));
                } catch (NoSuchComponentException e) {
                    //
                }
            }
        }

        return components;

    }

    public ComponentDTO getComponent(String organization, String componentId, boolean inflate)
            throws NoSuchComponentException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        ComponentDTO dto = componentService.getComponent(organization, componentId);

        if (inflate) {
            System.out.println("get component spaces for " + organization + " id " + componentId);
            // enrich component with spaces info
            dto.setSpaces(listComponentSpaces(organization, componentId));
            System.out.println("dump spaces " + dto.getSpaces().toString());

        }
        return dto;
    }

    public ComponentDTO addComponent(String organization, String componentId, String userId, List<String> roles)
            throws IdentityProviderAPIException, NoSuchUserException {
        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return componentService.addComponent(organization, componentId, userId, roles);
    }

    public ComponentDTO updateComponent(String organization, String componentId, String userId, String name,
            List<String> roles)
            throws NoSuchComponentException, IdentityProviderAPIException, NoSuchUserException {
        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // name is not saved
        // check roles
        ComponentDTO component = getComponent(organization, componentId, true);
        System.out.println("pre " + component.getSpaces().toString());
        if (roles != null) {
            // match config
            List<String> current = (component.getRoles() == null) ? new ArrayList<>() : component.getRoles();
            List<String> toRemove = new ArrayList<>();
            List<String> toAdd = new ArrayList<>();
            for (String role : current) {
                if (!roles.contains(role)) {
                    toRemove.add(role);
                }
            }
            for (String role : roles) {
                if (!current.contains(role)) {
                    toAdd.add(role);
                }
            }

            for (String role : toRemove) {
                componentService.deleteRole(organization, componentId, role);
            }

            for (String role : toAdd) {
                componentService.addRole(organization, componentId, role);
            }

            // re-read
            component = getComponent(organization, componentId, true);
        }
        System.out.println("post " + component.getSpaces().toString());

        return component;
    }

    public void deleteComponent(String organization, String componentId, boolean cleanup)
            throws IdentityProviderAPIException, NoSuchComponentException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        try {
            if (cleanup) {
                // remove all component spaces from this organization
                // also works for disabled/orphan components
                List<String> spaces = spaceService.listSpaces(organization);

                for (String space : spaces) {
                    try {
                        unregisterComponentSpace(organization, componentId, space, true);
                    } catch (NoSuchSpaceException e) {
                        // ignore
                    }
                }

            }

            // remove config
            componentService.deleteComponent(organization, componentId);
        } catch (NoSuchSpaceException e) {
            throw new NoSuchComponentException();
        }
    }

    /*
     * Org component roles
     */
    public List<String> addRole(String organization, String componentId, String role)
            throws IdentityProviderAPIException, NoSuchComponentException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        componentService.addRole(organization, componentId, role);

        return getComponent(organization, componentId, false).getRoles();

    }

    public List<String> deleteRole(String organization, String componentId, String role)
            throws IdentityProviderAPIException, NoSuchComponentException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        componentService.deleteRole(organization, componentId, role);

        return getComponent(organization, componentId, false).getRoles();

    }

    /*
     * Component spaces
     */
    public List<SpaceDTO> listComponentSpaces(String organization, String componentId)
            throws NoSuchComponentException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        List<String> cSpaces = componentService.listComponentSpaces(componentId);

        // fetch org spaces
        List<String> spaces = spaceService.listSpaces(organization);

        // check against registered spaces
        return spaces.stream()
                .filter(s -> cSpaces.contains(organization + Constants.SLUG_SEPARATOR + s))
                .map(s -> SpaceDTO.from(organization, s, s))
                .collect(Collectors.toList());

    }

    public List<SpaceDTO> updateComponentSpaces(String organization, String componentId, List<String> spaces)
            throws NoSuchComponentException, IdentityProviderAPIException, NoSuchSpaceException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // current spaces
        List<String> cSpaces = componentService.listComponentSpaces(componentId);

        // fetch org spaces
        List<String> oSpaces = spaceService.listSpaces(organization);

        // updates
//        List<String> toRemove = new ArrayList<>();
//        List<String> toAdd = new ArrayList<>();

        // ?
//        for (String s : cSpaces) {
//            if (!spaces.contains(s)) {
//                toAdd.add(s);
//            }
//        }

        List<String> toAdd = spaces.stream()
                .filter(s -> !cSpaces.contains(s))
                .filter(s -> oSpaces.contains(s))
                .collect(Collectors.toList());

        List<String> toRemove = cSpaces.stream()
                .filter(s -> !spaces.contains(s))
                .filter(s -> oSpaces.contains(s))
                .collect(Collectors.toList());

        for (String space : toRemove) {
            // perform cleanup of assignments
            unregisterComponentSpace(organization, componentId, space, true);
        }

        for (String space : toAdd) {
            // perform cleanup of assignments
            registerComponentSpace(organization, componentId, space);
        }

        return listComponentSpaces(organization, componentId);
    }

    public String registerComponentSpace(String organization, String componentId, String space)
            throws IdentityProviderAPIException, NoSuchSpaceException, NoSuchComponentException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // fetch owners
        String owner = spaceService.getSpaceOwner(organization, space);
        List<String> providers = new ArrayList<>(spaceService.getSpaceProviders(organization, space));

        return componentService.registerComponentSpace(organization, componentId, space, owner, providers);
    }

    public void unregisterComponentSpace(String organization, String componentId, String space, boolean cleanup)
            throws IdentityProviderAPIException, NoSuchSpaceException {

        // Admin or org owner/provider can manage org components
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        componentService.unregisterComponentSpace(organization, componentId, space, cleanup);
    }

}
