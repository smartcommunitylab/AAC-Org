package it.smartcommunitylab.orgmanager.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchOrganizationException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;

@Service
public class SpaceService {

    @Autowired
    private RoleService roleService;

    /*
     * Context
     */
    public String getOrgContext(String organization) {
        // spaces are listed in org sub-context
        return AACRoleDTO
                .concatContext(AACRoleDTO.ORGANIZATION_PREFIX + organization + AACRoleDTO.SPACES_PATH);
    }

    /*
     * Space handling
     */

    public List<SpaceDTO> listSpaces(String organization)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);
        return roleService.listSpaces(context).stream().map(r -> SpaceDTO.from(r))
                .collect(Collectors.toList());
    }

    public SpaceDTO addSpace(String organization, String space)
            throws IdentityProviderAPIException {
        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);
        ;

        // find org owner
        String owner = roleService.getSpaceOwner(AACRoleDTO.ORGANIZATION_PREFIX, organization);
        // add space
        AACRoleDTO spaceRole = roleService.addSpace(context, space, owner);

        // find all providers and enlist in space
        Set<String> providers = new HashSet<>(
                roleService.getSpaceProviders(AACRoleDTO.ORGANIZATION_PREFIX, organization));
        providers.add(owner);
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, space);

        for (String provider : providers) {
            // add for each provider an entry
            roleService.addRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        return SpaceDTO.from(spaceRole);
    }

    public void deleteSpace(String organization, String space)
            throws IdentityProviderAPIException {

        // Admin or org owner/provider can manage org spaces
        if (!OrgManagerUtils.userHasAdminRights()
                && !OrgManagerUtils.userIsOwner(organization)
                && !OrgManagerUtils.userIsProvider(organization)) {
            throw new AccessDeniedException("Access is denied: insufficient rights.");
        }

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        // find owner
        String owner = roleService.getSpaceOwner(context, space);

        // find all providers and clear in space
        Set<String> providers = new HashSet<>(
                roleService.getSpaceProviders(context, space));
        providers.add(owner);
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, space);

        for (String provider : providers) {
            // remove space role for each provider
            roleService.deleteRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        // remove space
        roleService.deleteSpace(context, space, owner);

    }

}
