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
    public List<ComponentDTO> listModels() {
        return componentService.listModels();
    }

    public ComponentDTO getModel(String component) throws NoSuchComponentException {
        return componentService.getModel(component);
    }

    /*
     * Org components
     */

    public List<ComponentDTO> listComponents(String organization)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org components
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return componentService.getComponents(organization);

    }

    public ComponentDTO getComponent(String organization, String componentId)
            throws NoSuchComponentException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return componentService.getComponent(organization, componentId);
    }

    public ComponentDTO addComponent(String organization, String componentId, String userName)
            throws IdentityProviderAPIException, NoSuchUserException {
        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return componentService.addComponent(organization, componentId, userName);
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

        return getComponent(organization, componentId).getRoles();

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

        return getComponent(organization, componentId).getRoles();

    }

    /*
     * Component spaces
     */
    public List<String> listComponentSpaces(String organization, String componentId)
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
                .collect(Collectors.toList());

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
