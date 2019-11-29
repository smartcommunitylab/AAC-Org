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

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.smartcommunitylab.aac.AACException;
import it.smartcommunitylab.aac.AACRoleService;
import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.aac.model.User;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;
import it.smartcommunitylab.orgmanager.config.SecurityConfig;
import it.smartcommunitylab.orgmanager.dto.AACRoleDTO;

/**
 * @author raman
 *
 */
@Service
@Transactional
public class RoleService {
    private final static Logger logger = LoggerFactory.getLogger(RoleService.class);

    private AACRoleService aacRoleService;

    @Autowired
    private SecurityConfig securityConfig;

    /**
     * Initializes the service to obtain client tokens.
     */
    @PostConstruct
    private void init() {
        // Generates the service to obtain the proper client tokens needed for certain
        // calls to the identity provider's APIs
        aacRoleService = securityConfig.getAACRoleService();
    }

    /**
     * @param slug
     * @return
     * @throws IdentityProviderAPIException
     */
    public Set<User> getOrganizationOwners(String slug) throws IdentityProviderAPIException {
        Role owner = AACRoleDTO.orgOwner(slug);
        try {
            logger.debug("get organization owners for slug " + slug);
            return aacRoleService.getSpaceUsers(owner.canonicalSpace(), owner.getRole(), 0, 1000, getToken());
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    /**
     * @param slug
     * @return
     * @throws IdentityProviderAPIException
     */
    public Set<User> getOrganizationMembers(String slug) throws IdentityProviderAPIException {
        Role member = AACRoleDTO.orgMember(slug);
        try {
            logger.debug("get organization members for slug " + slug);
            return aacRoleService.getSpaceUsers(member.canonicalSpace(), null, 0, 1000, getToken());
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    /**
     * @param canonicalSpace
     * @return
     * @throws IdentityProviderAPIException
     */
    public Set<User> getRoleUsers(String canonicalSpace) throws IdentityProviderAPIException {
        try {
            logger.debug("get role users for space " + canonicalSpace);
            return aacRoleService.getSpaceUsers(canonicalSpace, null, 0, 1000, getToken());
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    /**
     * @param rolesToAdd
     * @throws IdentityProviderAPIException
     */
    public void addRoles(Set<User> rolesToAdd) throws IdentityProviderAPIException {
        try {
            for (User user : rolesToAdd) {
                aacRoleService.addRoles(getToken(), user.getUserId(),
                        user.getRoles().stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
            }
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to add roles to user.");
        }
    }

    /**
     * @param rolesToRemove
     * @throws IdentityProviderAPIException
     */
    public void deleteRoles(Set<User> rolesToRemove) throws IdentityProviderAPIException {
        try {
            for (User user : rolesToRemove) {
                aacRoleService.deleteRoles(getToken(), user.getUserId(),
                        user.getRoles().stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
            }
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to delete user roles.");
        }
    }

    /**
     * @param username
     * @throws IdentityProviderAPIException
     */
    public Set<Role> getRoles(User user) throws IdentityProviderAPIException {
        try {
            logger.debug("get roles for  user " + user.getUserId());
            Set<Role> roles = aacRoleService.getRolesByUserId(getToken(), user.getUserId());
            return roles;
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve user roles.");
        }
    }

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
