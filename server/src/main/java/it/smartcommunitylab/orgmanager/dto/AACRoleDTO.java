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

package it.smartcommunitylab.orgmanager.dto;

import org.springframework.util.StringUtils;

import it.smartcommunitylab.aac.model.Role;
import it.smartcommunitylab.orgmanager.common.Constants;

/**
 * @author raman
 *
 */
@SuppressWarnings("serial")
public class AACRoleDTO extends Role {

	private static final String COMPONENTS_PREFIX = Constants.ROOT_COMPONENTS + "/";
	private static final String ORGANIZATION_PREFIX = Constants.ROOT_ORGANIZATIONS + "/";
	
	public AACRoleDTO() {
		super();
	}
	public AACRoleDTO(String context, String space, String role) {
		setContext(context);
		setSpace(space);
		setRole(role);
	}
	
	public String getAuthority() {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isEmpty(getContext())) {
			sb.append(getContext());
			sb.append('/');
		}
		if (!StringUtils.isEmpty(getSpace())) {
			sb.append(getSpace());
			sb.append(':');
		}
		sb.append(getRole());
		return sb.toString();
	}
	
	
	/**
	 * @param ga
	 * @return true if the roles in the same context space
	 */
	public static boolean matchContextSpace(Role role, String authority) {
		if (authority.indexOf(':') < 0) return role.getContext() == null && role.getSpace() == null;
		return role.canonicalSpace().equals(authority.substring(0, authority.indexOf(':')));
	}

	/**
	 * @param componentId
	 * @param name
	 * @return
	 */
	public static Role tenantOwner(String component, String name) {
		return new AACRoleDTO(COMPONENTS_PREFIX + component, name, Constants.ROLE_PROVIDER);
	}
	/**
	 * @param componentId
	 * @param name
	 * @return
	 */
	public static Role tenantUser(String component, String name) {
		return new AACRoleDTO(COMPONENTS_PREFIX + component, name, null);
	}
	/**
	 * @param slug
	 * @return
	 */
	public static Role orgMember(String name) {
		return new AACRoleDTO(Constants.ROOT_ORGANIZATIONS, name, Constants.ROLE_MEMBER);
	}
	
	public static boolean isComponentRole(Role role) {
		return role.getContext() != null && role.getContext().startsWith(COMPONENTS_PREFIX);
	}
	
	/**
	 * @param r
	 * @return
	 */
	public static boolean isOrgRole(Role role) {
		return role.getContext() != null && (role.getContext().equals(Constants.ROOT_ORGANIZATIONS) || role.getContext().startsWith(ORGANIZATION_PREFIX));
	}

	
	/**
	 * @param organizationManagementContext
	 * @param slug
	 * @return
	 */
	public static Role orgOwner(String name) {
		return new AACRoleDTO(Constants.ROOT_ORGANIZATIONS, name, Constants.ROLE_PROVIDER);
	}

	/**
	 * @param componentId
	 * @param slug
	 * @return
	 */
	public static Role componentOrgOwner(String componentId, String slug) {
		return concatRole(Constants.ROLE_PROVIDER, Constants.ROOT_COMPONENTS, componentId, slug);
	}

	/**
	 * @param organizationManagementContext
	 * @param slug
	 * @return
	 */
	public static Role resourceOwner(String name) {
		return new AACRoleDTO(Constants.ROOT_RESOURCES, name, Constants.ROLE_PROVIDER);
	}
	/**
	 * @param r
	 * @return
	 */
	public static String componentName(Role r) {
		return r.getContext().substring(r.getContext().lastIndexOf('/')+1);
	}
	/**
	 * @param root
	 * @param context
	 * @param space
	 * @param role
	 * @return
	 */
	public static Role concatRole(String role, String ... parts) {
		return Role.parse(StringUtils.arrayToDelimitedString(parts, "/") + ":"+ role);
	}
	
}
