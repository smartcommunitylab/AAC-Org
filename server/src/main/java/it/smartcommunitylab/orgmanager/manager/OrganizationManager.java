package it.smartcommunitylab.orgmanager.manager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.dto.OrganizationDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;
import it.smartcommunitylab.orgmanager.service.ComponentService;
import it.smartcommunitylab.orgmanager.service.OrganizationService;
import it.smartcommunitylab.orgmanager.service.SpaceService;

@Service
public class OrganizationManager {
    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private ComponentService componentService;

    /*
     * Organizations
     */

    public List<OrganizationDTO> listOrganizations()
            throws IdentityProviderAPIException {

        boolean asAdmin = false;

        // Admin can manage all org
        if (OrgManagerUtils.userHasAdminRights()
                || OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                || OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)) {
            asAdmin = true;
        }

        if (asAdmin) {
            return organizationService.listOrganizations();
        } else {
            // filter, org owner/provider can manage org
            return organizationService.listOrganizations().stream()
                    .filter(o -> (OrgManagerUtils.userIsOwner(o.getSlug())
                            || OrgManagerUtils.userIsProvider(o.getSlug())))
                    .collect(Collectors.toList());
        }

    }

    public OrganizationDTO getOrganization(String organization)
            throws NoSuchOrganizationException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        try {
            return organizationService.getOrganization(organization);
        } catch (NoSuchUserException e) {
            throw new NoSuchOrganizationException();
        }

    }

    public OrganizationDTO addOrganization(String organization, String userName)
            throws NoSuchUserException, IdentityProviderAPIException {

        // Admin or context owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return organizationService.addOrganization(organization, userName);
    }

    public void deleteOrganization(String organization, boolean cleanup)
            throws NoSuchOrganizationException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsProvider(Constants.ROOT_ORGANIZATIONS)
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // delete all spaces in config + components
        List<String> spaces = spaceService.listSpaces(organization);
        for (String space : spaces) {
            try {
                deleteSpace(organization, space, cleanup);
            } catch (NoSuchSpaceException e) {
                // skip
            }
        }

        // delete all components
        List<String> components = componentService.listComponents(organization);
        for (String componentId : components) {
            try {
                componentService.deleteComponent(organization, componentId);
            } catch (NoSuchSpaceException e) {
                // skip
            }
        }

        // TODO delete all resources
        //

        // delete org and members definitions
        organizationService.deleteOrganization(organization, cleanup);

    }

    /*
     * Org spaces
     */

    public List<SpaceDTO> listSpaces(String organization)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return spaceService.getSpaces(organization);

    }

    public SpaceDTO getSpace(String organization, String space)
            throws NoSuchSpaceException, IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        try {
            return spaceService.getSpace(organization, space);
        } catch (NoSuchUserException e) {
            throw new NoSuchSpaceException();
        }

    }

    public SpaceDTO addSpace(String organization, String space, String userName)
            throws IdentityProviderAPIException, NoSuchUserException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        return spaceService.addSpace(organization, space, userName);
    }

    public void deleteSpace(String organization, String space, boolean cleanup)
            throws IdentityProviderAPIException, NoSuchSpaceException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // delete all component spaces
        List<String> components = Collections.emptyList();
        if (cleanup) {
            // ensure cleanup on all components, even disabled
            components = componentService.listComponents();
        } else {
            // check only enabled components
            components = componentService.listComponents(organization);
        }

        for (String componentId : components) {
            try {
                componentService.unregisterComponentSpace(organization, componentId, space, cleanup);
            } catch (NoSuchSpaceException e) {
                // ignore, non existent
            }
        }

        // delete space definition
        spaceService.deleteSpace(organization, space, cleanup);

    }

}
