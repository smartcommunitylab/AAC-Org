/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/

package it.smartcommunitylab.orgmanager.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.smartcommunitylab.aac.AACException;
import it.smartcommunitylab.aac.AACRoleService;
import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.common.NoSuchUserException;
import it.smartcommunitylab.orgmanager.config.ModelsConfig.ComponentsConfiguration;
import it.smartcommunitylab.orgmanager.config.SecurityConfig;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;

/**
 * @author raman
 *
 */
@Service
public class RoleService {
    private final static Logger logger = LoggerFactory.getLogger(RoleService.class);

    private AACRoleService aacRoleService;

    @Autowired
    private SecurityConfig securityConfig;
//
//    @Autowired
//    private ComponentsConfiguration componentsConfiguration;
//
//    private static Set<String> componentIds;

    /**
     * Initializes the service to obtain client tokens.
     */
    @PostConstruct
    private void init() {
        // Generates the service to obtain the proper client tokens needed for certain
        // calls to the identity provider's APIs
        aacRoleService = securityConfig.getAACRoleService();
//        componentIds = componentsConfiguration.getComponents().stream().map(c -> c.get(Constants.FIELD_COMPONENT_ID))
//                .collect(Collectors.toSet());
    }

    /*
     * Context/Space handling: respect the mapping 1 owner + N providers PER space
     */
    public Collection<AACRoleDTO> listSpaces(String context) throws IdentityProviderAPIException {
        logger.debug("list spaces for context " + context);

        try {
            Role role = AACRoleDTO.ownerRole(context, null);
            return aacRoleService
                    .getSpaceUsers(context, role.getRole(), true, 0, 1000, getToken())
                    .stream()
                    // we need to filter since AAC returns garbage on search
                    .flatMap(u -> u.getRoles()
                            .stream()
                            .filter(r -> (context.equals(r.getContext())
                                    && role.getRole().equals(r.getRole()))))
                    .map(r -> AACRoleDTO.from(r))
                    .collect(Collectors.toSet());
        } catch (SecurityException | AACException e) {
            e.printStackTrace();
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    public AACRoleDTO addSpace(String context, String space, String owner) throws IdentityProviderAPIException {
        logger.debug("add space " + space + " for context " + context);

        try {
            Role role = AACRoleDTO.ownerRole(context, space);
            aacRoleService.addRoles(getToken(), owner, Collections.singletonList(role.getAuthority()));

            return AACRoleDTO.from(role);
        } catch (SecurityException | AACException e) {
            e.printStackTrace();
            throw new IdentityProviderAPIException("Failed to associate space to owner");
        }

    }

    public void deleteSpace(String context, String space, String owner) throws IdentityProviderAPIException {
        logger.debug("delete space " + space + " for context " + context);

        try {
            Role role = AACRoleDTO.ownerRole(context, space);
            aacRoleService.deleteRoles(getToken(), owner, Collections.singletonList(role.getAuthority()));
        } catch (SecurityException | AACException e) {
            e.printStackTrace();
            throw new IdentityProviderAPIException("Unable to call identity provider's API to delete user roles.");
        }
    }

    public String getSpaceOwner(String context, String space) throws NoSuchUserException, IdentityProviderAPIException {
        logger.debug("get owner for context " + context + " space " + String.valueOf(space));
        try {
            Role role = AACRoleDTO.ownerRole(context, space);
            Collection<User> users = aacRoleService.getSpaceUsers(role.canonicalSpace(), role.getRole(), false, 0, 1000,
                    getToken());
            if (users.isEmpty()) {
                throw new NoSuchUserException();
            } else {
                // we expect ONE owner per space
                return users.iterator().next().getUserId();
            }

        } catch (SecurityException | AACException e) {
            e.printStackTrace();
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    public Collection<String> getSpaceProviders(String context, String space) throws IdentityProviderAPIException {
        logger.debug("get providers for context " + context + " space " + String.valueOf(space));
        try {
            Role role = AACRoleDTO.providerRole(context, space);
            return aacRoleService.getSpaceUsers(role.canonicalSpace(), role.getRole(), false, 0, 1000, getToken())
                    .stream()
                    .map(r -> r.getUserId())
                    .collect(Collectors.toSet());

        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    public Collection<String> getSpaceUsers(String context, String space) throws IdentityProviderAPIException {
        logger.debug("get users for context " + context + " space " + String.valueOf(space));
        try {
            Role role = AACRoleDTO.providerRole(context, space);
            return aacRoleService.getSpaceUsers(role.canonicalSpace(), null, false, 0, 1000, getToken())
                    .stream()
                    .map(r -> r.getUserId())
                    .collect(Collectors.toSet());

        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    /*
     * User roles
     */
    public Collection<AACRoleDTO> getRoles(String userId) throws IdentityProviderAPIException {
        try {

            return aacRoleService.getRolesByUserId(getToken(), userId)
                    .stream()
                    .map(r -> AACRoleDTO.from(r))
                    .collect(Collectors.toList());

        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve user roles.");
        }
    }

    public void addRoles(String userId, List<String> roles) throws IdentityProviderAPIException {
        try {
            if (!roles.isEmpty()) {
                aacRoleService.addRoles(getToken(), userId, roles);
            }
        } catch (SecurityException | AACException e) {
            e.printStackTrace();
            throw new IdentityProviderAPIException("Unable to call identity provider's API to add roles to user.");
        }
    }

    public void deleteRoles(String userId, List<String> roles) throws IdentityProviderAPIException {
        try {
            if (!roles.isEmpty()) {
                aacRoleService.deleteRoles(getToken(), userId, roles);
            }
        } catch (SecurityException | AACException e) {
            e.printStackTrace();
            throw new IdentityProviderAPIException("Unable to call identity provider's API to delete user roles.");
        }
    }

//
//    /**
//     * @param slug
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<User> getOrganizationOwners(String slug) throws IdentityProviderAPIException {
//        Role owner = AACRoleDTO.orgOwner(slug);
//        try {
//            logger.debug("get organization owners for slug " + slug);
//            return new HashSet<>(
//                    aacRoleService.getSpaceUsers(owner.canonicalSpace(), owner.getRole(), false, 0, 1000, getToken()));
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
//        }
//    }
//
//    /**
//     * @param slug
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<User> getOrganizationMembers(String slug) throws IdentityProviderAPIException {
//        Role member = AACRoleDTO.orgMember(slug);
//        try {
//            logger.debug("get organization members for slug " + slug);
//            return new HashSet<>(
//                    aacRoleService.getSpaceUsers(member.canonicalSpace(), null, false, 0, 1000, getToken()));
//        } catch (SecurityException | AACException e) {
//            e.printStackTrace();
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
//        }
//    }
//
//    /**
//     * @param canonicalSpace
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<User> getRoleUsers(String canonicalSpace) throws IdentityProviderAPIException {
//        logger.debug("get role users for space " + canonicalSpace);
//        return getRoleUsers(canonicalSpace, null, false);
//    }
//
//    /**
//     * Parameterized role search: for subspaces and/or specific role.
//     * 
//     * @param canonicalSpace
//     * @param role
//     * @param nested
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<User> getRoleUsers(String canonicalSpace, String role, boolean nested)
//            throws IdentityProviderAPIException {
//        try {
//            logger.debug("get role users for space " + canonicalSpace);
//            Collection<User> users = aacRoleService.getSpaceUsers(canonicalSpace, role, nested, 0, 1000, getToken());
//            for (User u : users) {
//                u.setRoles(u.getRoles().stream().filter(r -> {
//                    String space = r.canonicalSpace();
//                    return (role == null || role.equals(r.getRole()))
//                            && (space.equals(canonicalSpace) || nested && space.startsWith(canonicalSpace + "/"));
//                }).collect(Collectors.toSet()));
//            }
//            return new HashSet<>(users);
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
//        }
//    }
//
//    /**
//     * Return set of prefixes for all the possible roles (resources, components,
//     * spaces) of the organizations
//     * 
//     * @param slug
//     * @return
//     */
//    public Set<String> getOrgPrefixes(String slug) {
//        Set<String> prefixes = new HashSet<>();
//        prefixes.add(Constants.ROOT_RESOURCES + "/" + slug);
//        prefixes.add(Constants.ROOT_ORGANIZATIONS + "/" + slug);
//        componentIds.forEach(comp -> {
//            prefixes.add(Constants.ROOT_COMPONENTS + "/" + comp + "/" + slug);
//        });
//        return prefixes;
//    }
//
//    /**
//     * 
//     * @param slug
//     * @return List of organization
//     * @throws IdentityProviderAPIException
//     * @throws AACException
//     * @throws SecurityException
//     */
//    public Set<String> getOrgs() throws IdentityProviderAPIException {
//        String context = Constants.ROOT_ORGANIZATIONS;
//        try {
//            return aacRoleService.getSpaceUsers(context, null, true, 0, 1000, getToken())
//                    .stream()
//                    .flatMap(u -> u.getRoles().stream().filter(r -> r.getContext().equals(context)))
//                    .map(r -> r.getSpace())
//                    .collect(Collectors.toSet());
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
//        }
//    }
//
//    /**
//     * Add space to an organization: set org owners as space owners
//     * 
//     * @param slug
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<String> addOrg(String slug, String owner) throws IdentityProviderAPIException {
//        String orgRole = AACRoleDTO.orgOwner(slug).getAuthority();
//        String token = getToken();
//
//        try {
//            aacRoleService.addRoles(token, owner, Collections.singletonList(orgRole));
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Failed to associate org to owner");
//        }
//
//        return getOrgSpaces(slug);
//    }
//
//    /**
//     * 
//     * @param slug
//     * @return List of spaces rooted to the organization
//     * @throws IdentityProviderAPIException
//     * @throws AACException
//     * @throws SecurityException
//     */
//    public Set<String> getOrgSpaces(String slug) throws IdentityProviderAPIException {
//        String context = Constants.ROOT_ORGANIZATIONS + "/" + slug;
//        try {
//            return aacRoleService
//                    .getSpaceUsers(context, null, true, 0, 1000, getToken())
//                    .stream()
//                    .flatMap(u -> u.getRoles().stream().filter(r -> r.getContext().equals(context)))
//                    .map(r -> r.getSpace())
//                    .collect(Collectors.toSet());
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
//        }
//    }
//
//    /**
//     * Add space to an organization: set org owners as space owners
//     * 
//     * @param slug
//     * @param space
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<String> addOrgSpace(String slug, String space) throws IdentityProviderAPIException {
//        String orgSpace = AACRoleDTO.orgOwner(slug).canonicalSpace();
//        String spaceRole = AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, orgSpace, space).getAuthority();
////    	String resourceRole = AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, Constants.ROOT_RESOURCES, slug, space).getAuthority();
//
//        String token = getToken();
//
//        Set<String> userIds = getOrganizationOwners(slug).stream().map(User::getUserId).collect(Collectors.toSet());
//        try {
//            for (String userId : userIds) {
////				aacRoleService.addRoles(token, userId, Arrays.asList(new String[] {spaceRole, resourceRole}));
//                aacRoleService.addRoles(token, userId, Collections.singletonList(spaceRole));
//            }
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Failed to associate org space to users");
//        }
//
//        return getOrgSpaces(slug);
//    }
//
//    /**
//     * Remove space from org: remove roles matching the org space and prefix spaces
//     * (e.g., resources and components)
//     * 
//     * @param slug
//     * @param space
//     * @param prefixes
//     * @return
//     * @throws IdentityProviderAPIException
//     */
//    public Set<String> removeOrgSpace(String slug, String space) throws IdentityProviderAPIException {
//        Set<String> canonicalSpaces = new HashSet<>();
//        Set<User> users = new HashSet<>();
//
//        Set<String> prefixes = getOrgPrefixes(slug);
//
//        // consider removing role for users in all additional spaces (e.g., components
//        // or resources)
//        for (String prefix : prefixes) {
//            Role prefixRole = AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, prefix, space);
//            String canonicalPrefixRoleSpace = prefixRole.canonicalSpace();
//            canonicalSpaces.add(canonicalPrefixRoleSpace);
//            users.addAll(getRoleUsers(canonicalPrefixRoleSpace));
//        }
//
//        String token = getToken();
//
//        try {
//            for (User user : users) {
//                aacRoleService.deleteRoles(token, user.getUserId(),
//                        user.getRoles().stream().filter(r -> canonicalSpaces.contains(r.canonicalSpace()))
//                                .map(r -> r.getAuthority()).collect(Collectors.toList()));
//            }
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Failed to associate org space to users");
//        }
//
//        return getOrgSpaces(slug);
//    }
//
//    /**
//     * @param rolesToAdd
//     * @throws IdentityProviderAPIException
//     */
//    public void addRoles(Set<User> rolesToAdd) throws IdentityProviderAPIException {
//        try {
//            String token = getToken();
//            for (User user : rolesToAdd) {
//                aacRoleService.addRoles(token, user.getUserId(),
//                        user.getRoles().stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
//            }
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to add roles to user.");
//        }
//    }
//
//    /**
//     * @param rolesToRemove
//     * @throws IdentityProviderAPIException
//     */
//    public void deleteRoles(Set<User> rolesToRemove) throws IdentityProviderAPIException {
//        try {
//            for (User user : rolesToRemove) {
//                aacRoleService.deleteRoles(getToken(), user.getUserId(),
//                        user.getRoles().stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
//            }
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to delete user roles.");
//        }
//    }
//
//    /**
//     * Return all the roles of the user with respect to the specified organization
//     * 
//     * @param username
//     * @param slug
//     * @throws IdentityProviderAPIException
//     */
//    public Set<Role> getRoles(User user, String slug) throws IdentityProviderAPIException {
//        try {
//            logger.debug("get roles for  user " + user.getUserId() + " with slug " + slug);
//            Collection<Role> roles = aacRoleService.getRolesByUserId(getToken(), user.getUserId());
//            // filter for the organization
//            Set<String> prefixes = getOrgPrefixes(slug);
//            roles = roles.stream().filter(r -> {
//                String canonical = r.canonicalSpace();
//                return prefixes.stream().anyMatch(p -> p.equals(canonical) || canonical.startsWith(p + "/"));
//            }).collect(Collectors.toSet());
//
//            return new HashSet<>(roles);
//        } catch (SecurityException | AACException e) {
//            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve user roles.");
//        }
//    }

    /**
     * Generates a client access token with the input scope.
     * 
     * @param scope - Scope the token needs to have
     * @return - Access token with the desired scope
     * @throws IdentityProviderAPIException
     */
    private String getToken() throws IdentityProviderAPIException {
        return securityConfig.getToken(Constants.SCOPE_MANAGE_ROLES);
    }

}
