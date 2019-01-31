package it.smartcommunitylab.orgmanager.common;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

import it.smartcommunitylab.aac.AACService;
import it.smartcommunitylab.aac.model.TokenData;
import it.smartcommunitylab.orgmanager.config.SecurityConfig;
import it.smartcommunitylab.orgmanager.model.Organization;
import it.smartcommunitylab.orgmanager.model.OrganizationMember;
import it.smartcommunitylab.orgmanager.model.Role;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
public class OrgManagerUtils {
	public static final String ROLE_PROVIDER = "ROLE_PROVIDER";
	public static final String ROOT_COMPONENTS = "components";
	public static final String ROOT_ORGANIZATIONS = "organizations";
	private static final String SCOPE_USER_PROFILES = "profile.basicprofile.all,profile.accountprofile.all";
	private static final String SCOPE_MANAGE_ROLES = "user.roles.write,user.roles.read,user.roles.read.all,client.roles.read.all";
	
	private AACService aacService;
	
	@Autowired
	private SecurityConfig securityConfig;
	
	/**
	 * Initializes the service to obtain client tokens.
	 */
	@PostConstruct
	private void init() {
		// Generates the service to obtain the proper client tokens needed for certain calls to the identity provider's APIs
		aacService = securityConfig.getAACService();
//		TokenData td; // TODO from here everything in this method is to quickly get a token for testing purposes
//		try {
//			td = aacService.generateUserToken("admin", "admin", "profile,email,profile.basicprofile.me,profile.accountprofile.me,user.roles.me");
//			System.out.println(td.getToken_type() + " " + td.getAccess_token());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Returns true if the authenticated user has admin rights.
	 * 
	 * @return - true if the authenticated user has admin rights, false otherwise
	 */
	public boolean userHasAdminRights() { // user has admin rights if they are admin or have the organization management scope
		return (userHasOrganizationMgmtScope() || userIsAdmin());
	}
	
	/**
	 * Returns true if the authenticated user is admin.
	 * User is admin if they have organizations:ROLE_PROVIDER role.
	 * 
	 * @return - True if the authenticated user is admin, false otherwise
	 */
	private boolean userIsAdmin() {
		HTTPResponse response = callIdentityProviderAPI(securityConfig.getCurrentUserRolesUri(), HTTPRequest.Method.GET, null);
		try {
			JSONArray responseJSON = response.getContentAsJSONArray();
			JSONObject roleJSON;
			String space, role;
			for (Object o : responseJSON) { // searches for the admin role
				roleJSON = (JSONObject) o;
				space = roleJSON.getAsString("space");
				role = roleJSON.getAsString("role");
				if (space != null && space.equals(ROOT_ORGANIZATIONS) && role != null && role.equals(ROLE_PROVIDER))
					return true; // authenticated user is admin
			}
			return false;
		} catch (ParseException e) {
			throw new IdentityProviderAPIException("API call to the identity provider to determine if the user is admin returned an unexpected response: " + e.getMessage());
		}
	}
	
	/**
	 * Checks whether or not the authenticated user has the organization management scope.
	 * 
	 * @return - true if the authenticated user has the organization management scope, false otherwise
	 */
	private boolean userHasOrganizationMgmtScope() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof OAuth2Authentication) {
			OAuth2Request request = ((OAuth2Authentication) authentication).getOAuth2Request();
			if (request != null) {
				Set<String> scopeSet = request.getScope();
				if (scopeSet != null) { // searches for the scope
					for (String scope : scopeSet) {
						if (scope.equalsIgnoreCase(securityConfig.getOrganizationManagementScope()))
							return true; // authenticated user has the organization management scope
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if the authenticated user is owner of the organization
	 * 
	 * @param organization - The organization whose owner might be the authenticated user
	 * @return - true if the authenticated user is owner, false otherwise
	 */
	public boolean userIsOwner(Organization organization) {
		HTTPResponse response = callIdentityProviderAPI(securityConfig.getCurrentUserRolesUri(), HTTPRequest.Method.GET, null);
		try {
			JSONArray responseJSON = response.getContentAsJSONArray();
			JSONObject roleJSON;
			String context, space, role;
			for (Object o : responseJSON) { // searches for the role that identifies ownership
				roleJSON = (JSONObject) o;
				context = roleJSON.getAsString("context");
				space = roleJSON.getAsString("space");
				role = roleJSON.getAsString("role");
				if (context != null && context.equals(ROOT_ORGANIZATIONS) && 
						space != null && space.equals(organization.getSlug())
						&& role != null && role.equals(ROLE_PROVIDER))
					return true; // authenticated user is owner
			}
			return false;
		} catch (ParseException e) {
			throw new IdentityProviderAPIException("API call to the identity provider to determine if the user is admin returned an unexpected response: " + e.getMessage());
		}
	}
	
	/**
	 * Returns true if the input role collection includes a role that identifies ownership of the organization.
	 * 
	 * @param roles - Collection of roles that may include owner role
	 * @param slug - Slug of the organization
	 * @return - True if the owner role could be found, false otherwise
	 */
	public boolean containsOwnerRole(Collection<Role> roles, String slug) {
		for (Role r : roles) {
			if (r.getRoleId().getContextSpace().equals(ROOT_ORGANIZATIONS + "/" + slug) &&
					r.getRoleId().getRole().equals(ROLE_PROVIDER))
				return true; // authenticated user is owner
		}
		return false;
	}
	
	/**
	 * Returns the ID used by the identity provider to identify the currently authenticated user.
	 * 
	 * @return - ID used by the identity provider to identify the currently authenticated user
	 */
	public String getAuthenticatedUserId() {
		Object obj = SecurityContextHolder.getContext().getAuthentication().getDetails(); // retrieves token
		if (!(obj instanceof OAuth2AuthenticationDetails)) { // cannot find token value, needed to find the ID
			throw new IdentityProviderAPIException("Unable to call identity provider's API to retrieve authenticated user's name.");
		}
		OAuth2AuthenticationDetails det = (OAuth2AuthenticationDetails) obj;
		String urlString = securityConfig.getTokenInfoUri() + "?" + securityConfig.getTokenName() + "=" + det.getTokenValue();
		HTTPResponse response = callIdentityProviderAPI(urlString, HTTPRequest.Method.POST, getTokenWithScope(SCOPE_USER_PROFILES)); // API call to get token info
		
		String userId = null;
		try {
			JSONObject responseJSON = response.getContentAsJSONObject();
			userId = responseJSON.getAsString(securityConfig.getUserIdField()); // ID of the authenticated user
		} catch (ParseException e) {
			throw new IdentityProviderAPIException("API call to the identity provider to find the authenticated user's ID returned an unexpected response: " + e.getMessage());
		}
		
		return userId;
	}
	
	/**
	 * Returns the user name used by the identity provider to identify the currently authenticated user.
	 * 
	 * @return - User name used by the identity provider to identify the currently authenticated user
	 */
	public String getAuthenticatedUserName() {
		HTTPResponse response = callIdentityProviderAPI(securityConfig.getCurrentUserProfileUri(), HTTPRequest.Method.GET, null);
		try {
			JSONObject responseJSON = response.getContentAsJSONObject();
			String userName = responseJSON.getAsString("username"); // user name of the authenticated user
			return userName;
		} catch (ParseException e) {
			throw new IdentityProviderAPIException("API call to the identity provider to find the authenticated user's name returned an unexpected response: " + e.getMessage());
		}
	}
	
	/**
	 * Returns the ID used by the identity provider to identify the user with the given username.
	 * 
	 * @param userName - Name of the user whose ID will be returned
	 * @return - ID of the input user
	 */
	public Long getUserId(String userName) {
		if (userName == null || userName.equals("")) // invalid request
			return null;
		String urlString = securityConfig.getUserProfilesUri() + "?username=" + userName;
		HTTPResponse httpResponse = callIdentityProviderAPI(urlString, HTTPRequest.Method.GET, getTokenWithScope(SCOPE_USER_PROFILES));
		try {
			JSONObject responseJSON = httpResponse.getContentAsJSONObject();
			JSONArray profiles = (JSONArray) responseJSON.get("profiles"); // a profiles array should be returned
			if (profiles == null || profiles.size() == 0 || profiles.get(0) == null) // no user found
				throw new EntityNotFoundException("Profile for user " + userName + " could not be found; unable to continue.");
			if (profiles.size() > 1) // Multiple users found, cannot determine which one is the right one
				throw new AmbiguousIdentifierException("The identity provider returned multiple profiles, cannot determine the correct user. Unable to continue.");
			JSONObject profile = (JSONObject) profiles.get(0);
			return new Long(profile.getAsString("userId")); // ID used by the identity provider
		} catch (ParseException e) {
			throw new IdentityProviderAPIException("API call to the identity provider to find " + userName + "'s ID returned an unexpected response: " + e.getMessage());
		}
	}
	
	public Map<OrganizationMember, List<Role>> createMemberToRolesMap(List<Object[]> memberRolesList) {
		Map<OrganizationMember, List<Role>> memberRolesMap = new HashMap<OrganizationMember, List<Role>>();
		List<Role> roles;
		OrganizationMember member;
		for (Object[] a : memberRolesList) { // builds a (member -> roles) map
			member = (OrganizationMember) a[0];
			roles = memberRolesMap.get(member);
			if (roles == null) { // member doesn't have a list of roles assigned yet
				roles = new ArrayList<Role>();
				memberRolesMap.put(member, roles);
			}
			roles.add((Role) a[1]);
		}
		return memberRolesMap;
	}
	
	/**
	 * Calls identity provider API to add a single role to a user.
	 * 
	 * @param userId - ID of the user
	 * @param role - Role to add
	 */
	public void idpAddRole(Long userId, Role role) {
		List<Role> roles = new ArrayList<Role>();
		roles.add(role); // list with just 1 element
		idpAddRoles(userId, roles); // calls method on the single-element list
	}
	
	/**
	 * Calls identity provider API to add roles to a user.
	 * 
	 * @param userId - ID of the user
	 * @param roles - Roles to add
	 */
	public void idpAddRoles(Long userId, Collection<Role> roles) {
		idpHandleRoles(userId, roles, HTTPRequest.Method.PUT);
	}
	
	/**
	 * Calls identity provider API to revoke roles from a user.
	 * 
	 * @param userId - ID of the user
	 * @param roles - Roles to remove
	 */
	public void idpRemoveRoles(Long userId, Collection<Role> roles) {
		idpHandleRoles(userId, roles, HTTPRequest.Method.DELETE);
	}
	
	/**
	 * Calls identity provider API to add or remove roles to/from a user.
	 * 
	 * @param userId - ID of the user
	 * @param roles - Roles to add/remove
	 * @param method - PUT or DELETE
	 */
	private void idpHandleRoles(Long userId, Collection<Role> roles, HTTPRequest.Method method) {
		if (roles == null || roles.isEmpty())
			return;
		String urlString = securityConfig.getManageRolesUri() + "/" + userId + "?roles=";
		Iterator<Role> iter = roles.iterator();
		while (iter.hasNext()) { // multiple roles can be handled at once
			urlString += iter.next();
			if (iter.hasNext()) // checks that it's not the last role
				urlString += ","; // separates roles with a comma
		}
		callIdentityProviderAPI(urlString, method, getTokenWithScope(SCOPE_MANAGE_ROLES));
	}
	
	/**
	 * Generates a client access token with the input scope.
	 * 
	 * @param scope - Scope the token needs to have
	 * @return - Access token with the desired scope
	 */
	private String getTokenWithScope(String scope) {
		try {
			TokenData dt = aacService.generateClientToken(scope);
			return dt.getToken_type() + " " + dt.getAccess_token();
		} catch (Exception e) {
			throw new IdentityProviderAPIException("Unable to generate an access token with the desired scope.");
		}
	}
	
	/**
	 * Calls one of the identity provider's APIs.
	 * 
	 * @param urlString - Endpoint to call
	 * @param method - GET, PUT, POST, DELETE, etc.
	 * @param token - Token to use for the request
	 * @return - The identity provider's response
	 */
	private static HTTPResponse callIdentityProviderAPI(String urlString, HTTPRequest.Method method, String token) {
		try {
			URL url = new URL(urlString); // generates a URL from the input string
			HTTPRequest httpRequest = new HTTPRequest(method, url);
			String accessToken = token;
			if (accessToken == null) { // if token was not specified, retrieves it from the context
				Object obj = SecurityContextHolder.getContext().getAuthentication().getDetails();
				if (obj instanceof OAuth2AuthenticationDetails) {
					OAuth2AuthenticationDetails det = (OAuth2AuthenticationDetails) obj;
					accessToken = det.getTokenType() + " " + det.getTokenValue();
				}
			}
			httpRequest.setAuthorization(accessToken); // sets authorization header
			HTTPResponse httpResponse = httpRequest.send();
			if (httpResponse.getStatusCode() >= 400) // some error occurred 
				throw new IOException(httpResponse.getStatusCode() + ": " + httpResponse.getStatusMessage());
			return httpResponse;
		} catch (IOException e) {
			throw new IdentityProviderAPIException("API call to the identity provider failed: " + e.getMessage());
		}
	}
}
