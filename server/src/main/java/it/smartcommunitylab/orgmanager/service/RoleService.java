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

	private AACRoleService aacRoleService;
	
	@Autowired
	private SecurityConfig securityConfig;
	
	/**
	 * Initializes the service to obtain client tokens.
	 */
	@PostConstruct
	private void init() {
		// Generates the service to obtain the proper client tokens needed for certain calls to the identity provider's APIs
		aacRoleService = securityConfig.getAACRoleService();
	}

	/**
	 * @param slug
	 * @return
	 */
	public Set<User> getOrganizationOwners(String slug) {
		Role owner = AACRoleDTO.orgOwner(slug);
		try {
			return aacRoleService.getSpaceUsers(owner.canonicalSpace(), owner.getRole(), 0, 1000, getToken());
		} catch (SecurityException | AACException e) {
			throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
		}
	}
	
	/**
	 * @param slug
	 * @return
	 */
	public Set<User> getOrganizationMembers(String slug) {
		Role member = AACRoleDTO.orgMember(slug);
		try {
			return aacRoleService.getSpaceUsers(member.canonicalSpace(), null, 0, 1000, getToken());
		} catch (SecurityException | AACException e) {
			throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
		}
	}


	/**
	 * @param canonicalSpace
	 * @return
	 */
	public Set<User> getRoleUsers(String canonicalSpace) {
		try {
			return aacRoleService.getSpaceUsers(canonicalSpace, null, 0, 1000, getToken());
		} catch (SecurityException | AACException e) {
			throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve users in role.");
		}
	}

	/**
	 * @param rolesToAdd
	 */
	public void addRoles(Set<User> rolesToAdd) {
		try {
			for (User user : rolesToAdd) {
				aacRoleService.addRoles(getToken(), user.getUserId(), user.getRoles().stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
			}
		} catch (SecurityException | AACException e) {
			throw new IdentityProviderAPIException("Unable to call identity provider's API to add roles to user.");
		}
	}

	/**
	 * @param rolesToRemove
	 */
	public void deleteRoles(Set<User> rolesToRemove) {
		try {
			for (User user : rolesToRemove) {
				aacRoleService.deleteRoles(getToken(), user.getUserId(), user.getRoles().stream().map(r -> r.getAuthority()).collect(Collectors.toList()));
			}
		} catch (SecurityException | AACException e) {
			throw new IdentityProviderAPIException("Unable to call identity provider's API to delete user roles.");
		}
	}

	/**
	 * @param username
	 */
	public Set<Role> getRoles(User user) {
		try {
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
	 */
	private String getToken() {
		return securityConfig.getToken(Constants.SCOPE_MANAGE_ROLES);
	}
	
}
