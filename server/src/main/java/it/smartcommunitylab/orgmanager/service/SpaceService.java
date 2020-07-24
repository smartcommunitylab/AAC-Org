package it.smartcommunitylab.orgmanager.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.aac.model.BasicProfile;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchSpaceException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;
import it.smartcommunitylab.orgmanager.dto.SpaceDTO;

@Service
public class SpaceService {
    private final static Logger logger = LoggerFactory.getLogger(SpaceService.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private ProfileService profileService;

    /*
     * Context
     */
    public String getOrgContext(String organization) {
        // spaces are listed in org sub-context
        return AACRoleDTO
                .concatContext(Constants.ROOT_ORGANIZATIONS, organization, Constants.ROOT_SPACES);
    }

    /*
     * Space handling
     */

    public List<String> listSpaces(String organization)
            throws IdentityProviderAPIException {

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);
        return roleService.listSpaces(context).stream().map(r -> r.getSpace())
                .collect(Collectors.toList());
    }

    public List<SpaceDTO> getSpaces(String organization)
            throws IdentityProviderAPIException {

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);
        return roleService.listSpaces(context).stream().map(r -> SpaceDTO.from(r))
                .collect(Collectors.toList());
    }

    public SpaceDTO getSpace(String organization, String space)
            throws NoSuchSpaceException, IdentityProviderAPIException, NoSuchUserException {

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        // find owner
        String ownerId = roleService.getSpaceOwner(context, space);
        if (ownerId == null) {
            throw new NoSuchSpaceException();
        }

        return new SpaceDTO(space, space, organization);

    }

    public SpaceDTO addSpace(String organization, String space, String userId)
            throws IdentityProviderAPIException, NoSuchUserException {

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        // DISABLED
//        // find org owner
//        String owner = roleService.getSpaceOwner(AACRoleDTO.ORGANIZATION_PREFIX, organization);

        // validate owner via idp
        BasicProfile profile = profileService.getUserProfileById(userId);

        logger.info("add space " + space + " org " + organization + " owner " + userId);

        // add space
        AACRoleDTO spaceRole = roleService.addSpace(context, space, profile.getUserId());

        // find all providers and enlist in space
        Set<String> providers = new HashSet<>(
                roleService.getSpaceProviders(Constants.ROOT_ORGANIZATIONS, organization));
        providers.add(profile.getUserId());
        AACRoleDTO providerRole = AACRoleDTO.providerRole(context, space);

        for (String provider : providers) {
            // add for each provider an entry
            roleService.addRoles(provider, Collections.singletonList(providerRole.getAuthority()));
        }

        return SpaceDTO.from(spaceRole);
    }

    public void deleteSpace(String organization, String space, boolean cleanup)
            throws IdentityProviderAPIException, NoSuchSpaceException {

        logger.info("delete space " + space + " org " + organization);

        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        try {
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

            if (cleanup) {
                // clear all custom roles defined in space
                Collection<String> users = roleService.getSpaceUsers(context, space);

                // parse each user and remove all space roles
                for (String user : users) {
                    List<String> rolesToDel = roleService
                            .getRoles(user).stream()
                            .filter(r -> (context.equals(r.getContext())
                                    && space.equals(r.getSpace())
                                    && !Constants.ROLE_OWNER.equals(r.getRole())))
                            .map(r -> r.getAuthority())
                            .collect(Collectors.toList());

                    logger.debug("remove space roles " + rolesToDel.toString() + " for user " + user);
                    roleService.deleteRoles(user, rolesToDel);
                }

            }

            // remove space
            roleService.deleteSpace(context, space, owner);

        } catch (NoSuchUserException e) {
            throw new NoSuchSpaceException();
        }

    }

    /*
     * Users
     */
    public String getSpaceOwner(String organization, String space)
            throws NoSuchSpaceException, IdentityProviderAPIException {
        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        try {
            return roleService.getSpaceOwner(context, space);
        } catch (NoSuchUserException e) {
            throw new NoSuchSpaceException();
        }
    }

    public Collection<String> getSpaceProviders(String organization, String space)
            throws NoSuchSpaceException, IdentityProviderAPIException {
        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        Collection<String> users = roleService.getSpaceProviders(context, space);
        if (users.isEmpty()) {
            throw new NoSuchSpaceException();
        }

        return users;
    }

    public Collection<String> getSpaceUsers(String organization, String space)
            throws NoSuchSpaceException, IdentityProviderAPIException {
        // spaces are listed in org sub-context
        String context = getOrgContext(organization);

        Collection<String> users = roleService.getSpaceUsers(context, space);
        if (users.isEmpty()) {
            throw new NoSuchSpaceException();
        }

        return users;
    }

}
