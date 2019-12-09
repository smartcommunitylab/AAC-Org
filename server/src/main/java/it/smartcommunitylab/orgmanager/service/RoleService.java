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

import java.util.Collections;
import java.util.HashSet;
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
            return aacRoleService.getSpaceUsers(owner.canonicalSpace(), owner.getRole(), false, 0, 1000, getToken());
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
            return aacRoleService.getSpaceUsers(member.canonicalSpace(), null, false, 0, 1000, getToken());
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
            return aacRoleService.getSpaceUsers(canonicalSpace, null, false, 0, 1000, getToken());
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    public Set<User> getRoleUsers(String canonicalSpace, String role, boolean nested) throws IdentityProviderAPIException {
        try {
            logger.debug("get role users for space " + canonicalSpace);
            return aacRoleService.getSpaceUsers(canonicalSpace, role, nested, 0, 1000, getToken());
        } catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
        }
    }

    /**
     * 
     * @param slug
     * @return List of spaces rooted to the organization
     * @throws IdentityProviderAPIException 
     * @throws AACException 
     * @throws SecurityException 
     */
    public Set<String> getOrgSpaces(String slug) throws IdentityProviderAPIException {
    	String space = AACRoleDTO.orgOwner(slug).canonicalSpace();
    	String prefix = space + "/";
    	try {
			return aacRoleService.getSpaceUsers(AACRoleDTO.orgOwner(slug).canonicalSpace(), null, true, 0, 1000, getToken())
			.stream()
			.flatMap(u -> u.getRoles().stream().map(r -> r.canonicalSpace()).filter(r -> r.startsWith(prefix)))
			.map(r -> r.substring(prefix.length()))
			.collect(Collectors.toSet());
		} catch (SecurityException | AACException e) {
            throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
		}
    }

    /**
     * Add space to an organization: set org owners as space owners
     * @param slug
     * @param space
     * @return
     * @throws IdentityProviderAPIException
     */
    public Set<String> addOrgSpace(String slug, String space) throws IdentityProviderAPIException {
    	String orgSpace = AACRoleDTO.orgOwner(slug).canonicalSpace();
    	String role = AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, orgSpace, space).getAuthority();
    	
    	String token = getToken();
    	
    	Set<String> userIds = getOrganizationOwners(slug).stream().map(User::getUserId).collect(Collectors.toSet());
    	try {
			for (String userId: userIds) {
				aacRoleService.addRoles(token, userId, Collections.singletonList(role));
			}
		} catch (SecurityException | AACException e) {
    		throw new IdentityProviderAPIException("Failed to associate org space to users");
		}
    	
    	return getOrgSpaces(slug);
    }

    
    /**
     * Remove space from org: remove roles matching the org space and prefix spaces (e.g., resources and components)
     * @param slug
     * @param space
     * @param prefixes
     * @return
     * @throws IdentityProviderAPIException
     */
    public Set<String> removeOrgSpace(String slug, String space, Set<String> prefixes) throws IdentityProviderAPIException {
    	Set<String> canonicalSpaces = new HashSet<>();
    	Set<User> users = new HashSet<>();
    	
    	// consider removing org space roles 
    	String orgSpace = AACRoleDTO.orgOwner(slug).canonicalSpace();
    	Role role = AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, orgSpace, space);
    	String roleCanonicalSpace = role.canonicalSpace();
    	users.addAll(getRoleUsers(roleCanonicalSpace));
    	canonicalSpaces.add(roleCanonicalSpace);

    	// consider removing role for users in all additional spaces (e.g., components or resources)
    	for (String prefix: prefixes) {
    		Role prefixRole = AACRoleDTO.concatRole(Constants.ROLE_PROVIDER, prefix, slug, space);
    		String canonicalPrefixRoleSpace = prefixRole.canonicalSpace();
    		canonicalSpaces.add(canonicalPrefixRoleSpace);
        	users.addAll(getRoleUsers(canonicalPrefixRoleSpace));
    	}
    	
    	String token = getToken();
    	
    	try {
			for (User user: users) {
				aacRoleService.deleteRoles(token, user.getUserId(), user.getRoles().stream().filter(r -> canonicalSpaces.contains(r.canonicalSpace())).map(r -> r.getAuthority()).collect(Collectors.toList()));
			}
		} catch (SecurityException | AACException e) {
    		throw new IdentityProviderAPIException("Failed to associate org space to users");
		}
    	
    	return getOrgSpaces(slug);
    }

    /**
     * @param rolesToAdd
     * @throws IdentityProviderAPIException
     */
    public void addRoles(Set<User> rolesToAdd) throws IdentityProviderAPIException {
        try {
        	String token = getToken();
            for (User user : rolesToAdd) {
                aacRoleService.addRoles(token, user.getUserId(),
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
