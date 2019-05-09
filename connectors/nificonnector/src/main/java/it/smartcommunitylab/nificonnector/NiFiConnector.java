package it.smartcommunitylab.nificonnector;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.ComponentException;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.DefaultComponentImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Service("it.smartcommunitylab.nificonnector.NiFiConnector")
public class NiFiConnector implements Component {
	
	/**
	 * Initializes several properties.
	 * 
	 * @param properties - Map: name of property -> value of property
	 */
	public String init(Map<String, String> properties) {
		NiFiConnectorUtils.init(properties);
		try {
			String errMessage = "Unable to verify if component " + NiFiConnectorUtils.getComponentId() + " is running.";
			callAPI(NiFiConnectorUtils.METHOD_GET, NiFiConnectorUtils.accessUrl(), null, errMessage);
		} catch (ComponentException e) {
			CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Initialization of " + NiFiConnectorUtils.getComponentId() + " connector failed. "
					+ "The component may not be running, or the connector's configuration is incorrect. If you want to "
					+ "disable the connector, replace the value " + NiFiConnectorUtils.getImplementation() + " from the "
					+ NiFiConnectorUtils.PROPERTY_IMPLEMENTATION + " property with " + DefaultComponentImpl.class.getName()
					+ ". Error: " +  e.getMessage());
		}
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "Initializaton complete.");
	}
	
	/**
	 * Creates a process group for the organization.
	 * 
	 * @param organizationName - Name of the organization
	 * @param ownerInfo - Owner of the organization
	 */
	public String createOrganization(String organizationName, UserInfo ownerInfo) {
		if (organizationName == null || organizationName.equals(""))
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "The organization to create was not specified.");
		JSONArray userGroups = listUserGroups();
		createUserGroups(null, organizationName, userGroups); // creates user groups related to the roles in the organization
		JSONObject processGroup = createProcessGroup(organizationName, NiFiConnectorUtils.ROOT); // the parent process group will be the root
		assignPolicies(processGroup, null, listUserGroups()); // null as organizationName parameter: the process group itself represents the organization
		assignRoleToUser(organizationName + ":" + NiFiConnectorUtils.getOwnerRole(), null, ownerInfo); // null as organizationName parameter: the process group itself represents the organization
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "Organization " + organizationName + " has been created with owner " + ownerInfo.getUsername() + ".");
	}
	
	/**
	 * Deletes the process group for the organization
	 * 
	 * @param organizationName - Name of the organization
	 * @param tenants - Tenants of the organization, unused
	 */
	public String deleteOrganization(String organizationName, List<String> tenants) {
		if (organizationName == null || organizationName.equals(""))
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "The organization to delete was not specified");
		JSONObject organizationPG = getProcessGroup(organizationName, NiFiConnectorUtils.ROOT); // the organization's process group
		if (organizationPG != null) {
			JSONArray nestedPGs = listProcessGroups(organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID)); // process groups nested inside the organization's process group
			JSONObject group, groupComponent;
			String groupName;
			for (Object o : nestedPGs) { // each nested process group will be deleted automatically when the organization's process group is deleted
				group = (JSONObject) o; // however, all user groups related to the nested process groups need to be deleted as well
				groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
				groupName = groupComponent.getAsString(NiFiConnectorUtils.FIELD_NAME);
				deleteUserGroups(groupName, organizationName); // deletes user groups of nested process group
			}
			deleteProcessGroup(organizationPG); // deletes the organization's process group
		}
		deleteUserGroups(null, organizationName); // deletes user groups of the organization's process group
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "Organization " + organizationName + " and all of its tenants have been deleted.");
	}
	
	/**
	 * Creates a user with the given name, if it does not exist already.
	 * 
	 * @param userInfo - User to create
	 */
	public String createUser(UserInfo userInfo) {
		String userName = userInfo.getUsername();
		JSONObject user = getUser(userName); // checks if user already exists
		if (user != null) // user already exists
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 1, "User " + userName + " already exists.");
		
		user = new JSONObject(); // user needs to be created
		
		JSONObject revision = new JSONObject(); // revision is required when creating new objects
		revision.appendField(NiFiConnectorUtils.FIELD_VERSION, 0);
		user.appendField(NiFiConnectorUtils.FIELD_REVISION, revision);
		
		JSONObject component = new JSONObject();
		component.appendField(NiFiConnectorUtils.FIELD_IDENTITY, userName); // name of the user
		user.appendField(NiFiConnectorUtils.FIELD_COMPONENT, component);
		
		String errMessage = "API call to create new user " + userName + " failed";
		user = callAPI(NiFiConnectorUtils.METHOD_POST, NiFiConnectorUtils.createUserUrl(), user, errMessage); // calls API to create user
		
		// Grants the user permission to view UI
		JSONObject viewUIPolicy = getPolicy(NiFiConnectorUtils.ACTION_READ, NiFiConnectorUtils.FLOW);
		addUserToPolicy(user, viewUIPolicy);
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "User " + userName + " was created.");
	}
	
	/**
	 * Removes a user from an organization.
	 * 
	 * @param userInfo - User to remove
	 * @param organizationName - Name of the organization
	 * @param tenants - List of tenants belonging to the organization, unused
	 */
	public String removeUserFromOrganization(UserInfo userInfo, String organizationName, List<String> tenants) {
		if (userInfo == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "The user to remove was not specified.");
		JSONObject user = getUser(userInfo.getUsername());
		if (user == null) // user does not belong to the organization
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 1, "User " + userInfo.getUsername() + " does not belong to the organization.");
		JSONObject organizationPG = getProcessGroup(organizationName, NiFiConnectorUtils.ROOT);
		if (organizationPG == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Process group for organization " + organizationName + " could not be found.");
		
		String organizationPGID = organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the organization's process group
		JSONArray nestedPGs = listProcessGroups(organizationPGID); // lists all process groups nested within the organization's process group
		JSONObject group, groupComponent;
		String groupName;
		for (Object o : nestedPGs) { // all roles for tenants of the organization are revoked from the user
			group = (JSONObject) o;
			groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
			groupName = groupComponent.getAsString(NiFiConnectorUtils.FIELD_NAME);
			for (String r : NiFiConnectorUtils.getPossibleRoles())
				revokeRoleFromUser(groupName + ":" + r, organizationName, userInfo);
		}
		for (String r : NiFiConnectorUtils.getPossibleRoles()) // all roles for the organization's process group are revoked from the user
			revokeRoleFromUser(organizationName + ":" + r, null, userInfo);
		
		// Revokes permission to view the organization's process group
		JSONObject viewOrganizationPGPolicy = getPolicy(NiFiConnectorUtils.ACTION_READ, NiFiConnectorUtils.TYPE_PROCESS_GROUP + organizationPGID);
		removeUserFromPolicy(user, viewOrganizationPGPolicy);
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "User " + userInfo.getUsername() + " was removed from organization " + organizationName + ".");
	}
	
	/**
	 * Assigns a role to a user.
	 * 
	 * @param role - Role to assign
	 * @param organization - Name of the organization
	 * @param userInfo - User to give the role to
	 */
	public String assignRoleToUser(String role, String organizationName, UserInfo userInfo) {
		if (userInfo == null || role == null || role.equals(""))
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "User or role to assign were not specified.");
		JSONObject user = getUser(userInfo.getUsername());
		if (user == null) // user cannot be found
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "User " + userInfo.getUsername() + " could not be found.");
		String roleToAssign = role;
		if (organizationName != null)
			roleToAssign = organizationName + "/" + roleToAssign;
		JSONArray userGroups = listUserGroups(); // lists all user groups
		JSONObject group = getUserGroup(roleToAssign, userGroups); // retrieves the user group corresponding to the input
		addUserToUserGroup(user, group); // adds the user to the user group
		
		// Grants permission to view the organization process group, if not already granted
		String organizationPGName = organizationName;
		if (organizationPGName == null) // happens when the role being assigned is for the organization's own process group
			organizationPGName = role.substring(0, role.indexOf(":")); // organization name is contained in the role
		JSONObject organizationPG = getProcessGroup(organizationPGName, NiFiConnectorUtils.ROOT);
		JSONObject viewOrganizationPGPolicy = getPolicy(NiFiConnectorUtils.ACTION_READ, // policy to view the process group
				NiFiConnectorUtils.TYPE_PROCESS_GROUP + organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID));
		addUserToPolicy(user, viewOrganizationPGPolicy);
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "User " + userInfo.getUsername() + " was assigned the role " + role + " in organization " + organizationName + ".");
	}
	
	/**
	 * Adds a user to a user group.
	 * 
	 * @param user - User to add
	 * @param group - User group
	 */
	private void addUserToUserGroup(JSONObject user, JSONObject group) {
		if (user == null || group == null)
			return;
		JSONObject groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
		String groupId  = group.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the user group
		String groupName = groupComponent.getAsString(NiFiConnectorUtils.FIELD_IDENTITY); // name of the user group
		JSONArray users = (JSONArray) groupComponent.get(NiFiConnectorUtils.FIELD_USERS); // members of the user group
		JSONObject providedUser = provideElementForSubArray(user);
		
		JSONObject userInGroup;
		String  userInGroupId;
		boolean found = false;
		for (Object o : users) { // checks if the user already belongs to the user group
			userInGroup = (JSONObject) o;
			userInGroupId = userInGroup.getAsString(NiFiConnectorUtils.FIELD_ID);
			if (userInGroupId.equals(user.getAsString(NiFiConnectorUtils.FIELD_ID))) { // user already belongs to the user group
				found = true;
				break;
			}
		}
		
		if (!found) {
			users.add(providedUser); // adds the user
			updateUserGroup(groupId, groupName, group); // updates the group
		}
	}
	
	/**
	 * Revokes a role from a user.
	 * 
	 * @param role - Role to revoke
	 * @param organizationName - Name of the organization
	 * @param userInfo - User to revoke the role from
	 */
	public String revokeRoleFromUser(String role, String organizationName, UserInfo userInfo) {
		if (userInfo == null || role == null || role.equals(""))
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "User or role to revoke were not specified.");
		JSONArray userGroups = listUserGroups(); // lists all user groups
		String roleToRevoke = role;
		if (organizationName != null)
			roleToRevoke = organizationName + "/" + roleToRevoke;
		JSONObject group = getUserGroup(roleToRevoke, userGroups); // retrieves the user group corresponding to the input
		if (group == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "User group for role " + role + " could not be found.");
		JSONObject groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
		JSONArray users = (JSONArray) groupComponent.get(NiFiConnectorUtils.FIELD_USERS); // members of the user group
		Object toRemove = null;
		JSONObject userInGroup, userInGroupComponent;
		String userInGroupName;
		for (Object u : users) { // searches for the right user to remove
			userInGroup = (JSONObject) u;
			userInGroupComponent = (JSONObject) userInGroup.get(NiFiConnectorUtils.FIELD_COMPONENT);
			userInGroupName = userInGroupComponent.getAsString(NiFiConnectorUtils.FIELD_IDENTITY);
			if (userInGroupName.equals(userInfo.getUsername())) { // this is the user to remove
				toRemove = u;
				break;
			}
		}
		if (toRemove != null) { // user was found and needs to be removed
			users.remove(toRemove); // removes the user
			String groupId = group.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the user group
			updateUserGroup(groupId, roleToRevoke, group); // updates the group
		}
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "User " + userInfo.getUsername() + " no longer has the " + role + " role.");
	}
	
	/**
	 * Creates a process group to represent the tenant.
	 * The process group is placed inside another process group, which uses the organization's name.
	 * Policies are created for the new process group, which restrict the operations to specific user groups.
	 * 
	 * @param tenantName - Name of the process group to create
	 * @param organizationName - Name of the organization which owns the tenant
	 * @param ownerInfo - Unused
	 */
	public String createTenant(String tenantName, String organizationName, UserInfo ownerInfo) {
		if (tenantName == null || tenantName.equals("") || organizationName == null || organizationName.equals(""))
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Tenant to create or its organization were not specified.");
		JSONObject organizationPG = getProcessGroup(organizationName, NiFiConnectorUtils.ROOT); // process group that represents the organization
		if (organizationPG == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Unable to create tenant; process group for the organization " + organizationName + " could not be found.");
		
		JSONArray userGroups = listUserGroups(); // lists all user groups
		createUserGroups(tenantName, organizationName, userGroups); // creates user groups for the tenant
		String organizationPGID = organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the organization's process group
		JSONObject tenantPG = createProcessGroup(tenantName, organizationPGID); // creates the process group
		assignPolicies(tenantPG, organizationName, userGroups); // creates the policies for the process group
		for (String ownerName : listOwners(organizationName)) // owners of the organization are added to the proper user group
			assignRoleToUser(tenantName + ":" + NiFiConnectorUtils.getOwnerRole(), organizationName, new UserInfo(ownerName, null, null));
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "Tenant " + tenantName + " has been created under organization " + organizationName + ".");
	}
	
	/**
	 * Deletes the process group that represent the tenant.
	 * User groups related to the process group are also deleted.
	 * 
	 * @param tenantName - Name of the process group to delete
	 * @param organizationName - Name of the organization which owns the tenant
	 */
	public String deleteTenant(String tenantName, String organizationName) {
		if (tenantName == null || tenantName.equals("") || organizationName == null || organizationName.equals(""))
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Tenant to delete or organization were not specified.");
		JSONObject organizationPG = getProcessGroup(organizationName, NiFiConnectorUtils.ROOT); // process group that represents the organization
		if (organizationPG == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Unable to delete tenant; process group for organization " + organizationName + " could not be found.");
		JSONObject tenantPG = getProcessGroup(tenantName, organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID)); // process group that represents the tenant
		if (tenantPG != null)
			deleteProcessGroup(tenantPG); // deletes the tenant's process group
		deleteUserGroups(tenantName, organizationName); // deletes user groups related to the tenant's process group
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, "Tenant " + tenantName + " under organization " + organizationName + " has been deleted.");
	}
	
	/**
	 * Creates a process group.
	 * 
	 * @param processGroupName - Name of the process group to create
	 * @param parentId - ID of parent process group: use "root" to nest the new process group inside the root one
	 * @return - The newly created process group
	 */
	private JSONObject createProcessGroup(String processGroupName, String parentId) {
		JSONObject processGroup = getProcessGroup(processGroupName, parentId);
		if (processGroup != null) // process group already exists
			return processGroup;
		processGroup = new JSONObject();
		
		JSONObject revision = new JSONObject(); // revision is required when creating new objects
		revision.appendField(NiFiConnectorUtils.FIELD_VERSION, 0);
		processGroup.appendField(NiFiConnectorUtils.FIELD_REVISION, revision);
		
		JSONObject component = new JSONObject();
		component.appendField(NiFiConnectorUtils.FIELD_NAME, processGroupName); // name of the process group
		setPosition(component); // determines the coordinates where the process group will be placed
		processGroup.appendField(NiFiConnectorUtils.FIELD_COMPONENT, component);
		
		String errMessage = "API call to create new process group \"" + processGroupName + "\" failed";
		return callAPI(NiFiConnectorUtils.METHOD_POST, NiFiConnectorUtils.createProcessGroupUrl(parentId), processGroup, errMessage);
	}
	
	/**
	 * Deletes a process group.
	 * 
	 * @param group - Process group to delete
	 */
	private void deleteProcessGroup(JSONObject group) {
		JSONObject revision = (JSONObject) group.get(NiFiConnectorUtils.FIELD_REVISION);
		String version = revision.getAsString(NiFiConnectorUtils.FIELD_VERSION); // specifying the version is important to delete a process group
		String groupId = group.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the process group
		JSONObject component = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
		String groupName = component.getAsString(NiFiConnectorUtils.FIELD_NAME); // name of the process group
		String errMessage = "API call to delete process group\"" + groupName + "\" failed";
		callAPI(NiFiConnectorUtils.METHOD_DELETE, NiFiConnectorUtils.deleteProcessGroupUrl(groupId, version), null, errMessage);
	}
	
	/**
	 * Returns the process group with the input name, among the child process groups of the input parent.
	 * 
	 * @param processGroupName - Name of the process group to find
	 * @param parentId - ID of the parent process group
	 * @return - The process group, or null if it was not found
	 */
	private JSONObject getProcessGroup(String processGroupName, String parentId) {
		JSONArray processGroups = listProcessGroups(parentId); // all process groups of the input parent process group
		if (processGroups == null) // no nested process groups
			return null;
		JSONObject group, groupComponent;
		String name;
		for (Object pg : processGroups) { // searches for the desired process group
			group = (JSONObject) pg;
			groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
			name = groupComponent.getAsString(NiFiConnectorUtils.FIELD_NAME); // name of the process group
			if (name.equals(processGroupName)) // it's the desired process group
				return group;
		}
		return null; // process group not found
	}
	
	/**
	 * Lists all process groups nested within the input one.
	 * 
	 * @param parentId - ID of parent process group: use "root" to list all process groups inside the root one
	 * @return - Array of the obtained process groups
	 */
	private JSONArray listProcessGroups(String parentId) {
		String errMessage = "API call to list process groups failed";
		JSONArray processGroups = new JSONArray();
		JSONObject responseAPI = callAPI(NiFiConnectorUtils.METHOD_GET, NiFiConnectorUtils.listProcessGroupsUrl(parentId), null, errMessage);
		if (responseAPI != null && responseAPI.get("processGroups") != null) // nested process groups were found
			processGroups.addAll((JSONArray) responseAPI.get(NiFiConnectorUtils.FIELD_PROCESSGROUPS));
		return processGroups;
	}
	
	/**
	 * Adds a user to a policy.
	 * 
	 * @param user - User to add
	 * @param policy - Policy to add the user to
	 */
	private void addUserToPolicy(JSONObject user, JSONObject policy) {
		if (user == null || policy == null)
			return;
		
		String userId = user.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the user
		JSONObject policyComponent = (JSONObject) policy.get(NiFiConnectorUtils.FIELD_COMPONENT);
		JSONArray users = (JSONArray) policyComponent.get(NiFiConnectorUtils.FIELD_USERS); // list of users/groups included in the policy
		for (Object u : users) {
			if (((JSONObject) u).get(NiFiConnectorUtils.FIELD_ID).equals(userId)) // already present
				return; // nothing to alter
		}
		
		users.add(provideElementForSubArray(user)); // adds user to list of users
		updatePolicy(policy.getAsString(NiFiConnectorUtils.FIELD_ID), policy); // updates policy
	}
	
	
	/**
	 * Removes a user from a policy.
	 * 
	 * @param user - User to remove
	 * @param policy - Policy to remove the user from
	 */
	private void removeUserFromPolicy(JSONObject user, JSONObject policy) {
		if (user == null || policy == null)
			return;
		
		String userId = user.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the user
		JSONObject policyComponent = (JSONObject) policy.get(NiFiConnectorUtils.FIELD_COMPONENT);
		JSONArray users = (JSONArray) policyComponent.get(NiFiConnectorUtils.FIELD_USERS); // list of users/groups included in the policy
		Object toRemove = null;
		for (Object u : users) {
			if (((JSONObject) u).get(NiFiConnectorUtils.FIELD_ID).equals(userId)) { // this is the user to remove
				toRemove = u;
				break;
			}
		}
		if (toRemove != null) { // user was found and needs to be removed
			users.remove(toRemove); // removes the user from the policy
			updatePolicy(policy.getAsString(NiFiConnectorUtils.FIELD_ID), policy); // updates the policy
		}
		
	}
	
	/**
	 * Assigns policies to a process group.
	 * 
	 * @param processGroup - Process group to create policies for
	 * @param organizationName - Name of the organization the process group belongs to
	 * @param userGroups - Array of all existing user groups
	 */
	private void assignPolicies(JSONObject processGroup, String organizationName, JSONArray userGroups) {
		createPolicy(NiFiConnectorUtils.TYPE_PROCESS_GROUP, processGroup, organizationName, NiFiConnectorUtils.ACTION_READ, userGroups); // policy to view the process group
		createPolicy(NiFiConnectorUtils.TYPE_PROCESS_GROUP, processGroup, organizationName, NiFiConnectorUtils.ACTION_WRITE, userGroups); // policy to modify the flow of the process group
		createPolicy(NiFiConnectorUtils.TYPE_OPERATION, processGroup, organizationName, NiFiConnectorUtils.ACTION_WRITE, userGroups); // policy to operate (run, stop, etc.) the process group
		createPolicy(NiFiConnectorUtils.TYPE_PROVENANCE, processGroup, organizationName, NiFiConnectorUtils.ACTION_READ, userGroups); // policy to view provenance events
		createPolicy(NiFiConnectorUtils.TYPE_DATA, processGroup, organizationName, NiFiConnectorUtils.ACTION_READ, userGroups); // policy to view metadata and content of queues
		createPolicy(NiFiConnectorUtils.TYPE_DATA, processGroup, organizationName, NiFiConnectorUtils.ACTION_WRITE, userGroups); // policy to empty queues and submit replays
	}
	
	/**
	 * Returns the policy for the given resource.
	 * 
	 * @param action - read/write
	 * @param resource - Resource to get the policy for
	 * @return - Policy for action on resource
	 */
	private JSONObject getPolicy(String action, String resource) {
		String errMessage = "API call to get policy for resource " + resource + " (" + action + ") failed";
		JSONObject policy;
		try {
			policy = callAPI(NiFiConnectorUtils.METHOD_GET, NiFiConnectorUtils.getPolicyUrl(action, resource), null, errMessage);
		} catch (ComponentException e) {
			policy = null;
		}
		if (policy != null) {
			JSONObject component = (JSONObject) policy.get(NiFiConnectorUtils.FIELD_COMPONENT);
			String policyResource = component.getAsString(NiFiConnectorUtils.FIELD_RESOURCE);
			if (!policyResource.equals(resource))
				return null;
		}
		return policy;
	}
	
	/**
	 * Creates a new policy for a process group.
	 * 
	 * @param resourceType - Type of the resource
	 * @param processGroup - Process group to create policy for
	 * @param organizationName - Name of the organization the process group belongs to
	 * @param action - read/write
	 * @param userGroups - Array of all user groups
	 * @return - the newly created policy
	 */
	private JSONObject createPolicy(String resourceType, JSONObject processGroup, String organizationName, String action, JSONArray userGroups) {
		JSONObject processGroupComponent = (JSONObject) processGroup.get(NiFiConnectorUtils.FIELD_COMPONENT);
		String processGroupName = processGroupComponent.getAsString(NiFiConnectorUtils.FIELD_NAME);
		String processGroupId = processGroup.getAsString(NiFiConnectorUtils.FIELD_ID);
		JSONArray newUserGroups = provideUserGroupsForPolicy(processGroupName, organizationName, action, userGroups); // list of groups with this permission
		
		boolean policyAlreadyExists = false;
		JSONObject policy = getPolicy(action, resourceType + processGroupId);
		JSONObject component;
		if (policy == null) {
			policy = new JSONObject();
		
			JSONObject revision = new JSONObject(); // revision is required when creating new objects
			revision.appendField(NiFiConnectorUtils.FIELD_VERSION, 0);
			policy.appendField(NiFiConnectorUtils.FIELD_REVISION, revision);
		
			component = new JSONObject();
			component.appendField(NiFiConnectorUtils.FIELD_RESOURCE, resourceType + processGroupId); // defines the resource the policy is for
			component.appendField(NiFiConnectorUtils.FIELD_ACTION, action); // defines whether it's a policy for reads or writes
		} else {
			policyAlreadyExists = true;
			component = (JSONObject) policy.get(NiFiConnectorUtils.FIELD_COMPONENT);
		}
		component.appendField(NiFiConnectorUtils.FIELD_USERGROUPS, newUserGroups);
		
		// The same permission is also granted to admin user
		JSONArray adminUsers = new JSONArray();
		JSONObject admin = getUser(NiFiConnectorUtils.getAdminName()); // gets the JSONObject representing admin user
		adminUsers.add(provideElementForSubArray(admin));
		component.appendField(NiFiConnectorUtils.FIELD_USERS, adminUsers); // the list of users only contains admin
		
		policy.appendField(NiFiConnectorUtils.FIELD_COMPONENT, component);
		String errMessage = "API call to create new policy for " + resourceType + processGroup.getAsString(NiFiConnectorUtils.FIELD_ID) + " (" + action + ") failed";
		if (!policyAlreadyExists)
			return callAPI(NiFiConnectorUtils.METHOD_POST, NiFiConnectorUtils.createPolicyUrl(), policy, errMessage);
		return updatePolicy(policy.getAsString(NiFiConnectorUtils.FIELD_ID), policy); // updates the policy
	}
	
	/**
	 * Updates the policy identified by the input ID.
	 * 
	 * @param id - ID of the policy
	 * @param policy - JSONObject containing the changes
	 * @return - The updated policy
	 */
	private JSONObject updatePolicy(String id, JSONObject policy) {
		String errMessage = "API call to update policy " + id + " failed";
		return callAPI(NiFiConnectorUtils.METHOD_PUT, NiFiConnectorUtils.updatePolicyUrl(id), policy, errMessage);
	}
	
	/**
	 * Creates all user groups for a specific tenant.
	 * 
	 * @param tenantName - Name of the tenant
	 * @param organizationName - Name of the organization that owns the tenant
	 * @param userGroups - Array of all existing user groups
	 */
	private void createUserGroups(String tenantName, String organizationName, JSONArray userGroups) {
		JSONObject revision = new JSONObject(); // revision is required when creating new objects
		revision.appendField(NiFiConnectorUtils.FIELD_VERSION, 0);
		String errMessage;
		
		for (String r : NiFiConnectorUtils.getPossibleRoles()) {
			String groupName = organizationName;
			if (tenantName != null)
				groupName += "/" + tenantName;
			groupName += ":" + r;
			JSONObject group = getUserGroup(groupName, userGroups); // checks if the user group already exists
			if (group != null) // user group already exists
				continue;
			
			group = new JSONObject();
			JSONObject component = new JSONObject();
			component.appendField(NiFiConnectorUtils.FIELD_IDENTITY, groupName); // sets the name of the user group
			group.appendField(NiFiConnectorUtils.FIELD_COMPONENT, component);
			group.appendField(NiFiConnectorUtils.FIELD_REVISION, revision);
			
			errMessage = "API call to create user group " + groupName + " failed";
			group = callAPI(NiFiConnectorUtils.METHOD_POST, NiFiConnectorUtils.createUserGroupUrl(), group, errMessage);
			userGroups.add(group); // user group has been created
		}
	}
	
	/**
	 * Deletes all user groups related to a tenant.
	 * 
	 * @param tenantName - Name of the tenant
	 * @param organizationName - Name of the organization that owns the tenant
	 */
	private void deleteUserGroups(String tenantName, String organizationName) {
		JSONArray userGroups = listUserGroups();
		String errMessage, groupName, groupId;
		for (String r : NiFiConnectorUtils.getPossibleRoles()) { // all user groups related to the tenant must be deleted
			groupName = organizationName;
			if (tenantName != null)
				groupName += "/" + tenantName;
			groupName += ":" + r;
			JSONObject group = getUserGroup(groupName, userGroups); // retrieves the user group to be deleted
			if (group == null) // user group was not found, no need to delete it
				continue;
			JSONObject revision = (JSONObject) group.get(NiFiConnectorUtils.FIELD_REVISION);
			String version = revision.getAsString(NiFiConnectorUtils.FIELD_VERSION);
			groupId = group.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the user group to delete
			errMessage = "API call to delete user group " + groupName + " failed";
			callAPI(NiFiConnectorUtils.METHOD_DELETE, NiFiConnectorUtils.deleteUserGroupUrl(groupId, version), null, errMessage);
		}
	}
	
	/**
	 * Returns the user group with the input name.
	 * 
	 * @param groupToFind - Name of the user group to find
	 * @param userGroups - Existing user groups
	 * @return - User group corresponding to the input name
	 */
	private JSONObject getUserGroup(String groupToFind, JSONArray userGroups) {
		if (groupToFind == null || groupToFind.equals("") || userGroups == null)
			return null;
		JSONObject group, groupComponent;
		String groupName;
		for (Object g : userGroups) { // searches for the right user group
			group = (JSONObject) g;
			groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
			groupName = groupComponent.getAsString(NiFiConnectorUtils.FIELD_IDENTITY); // name of the user group
			if (groupName.equals(groupToFind)) // user group has been found
				return group;
		}
		return null; // cannot find user group
	}
	
	/**
	 * Lists all user groups.
	 * 
	 * @return - Array of all user groups
	 */
	private JSONArray listUserGroups() {
		String errMessage = "API call to list user groups failed";
		JSONObject responseAPI = callAPI(NiFiConnectorUtils.METHOD_GET, NiFiConnectorUtils.listUserGroupsUrl(), null, errMessage);
		
		JSONArray groups = (JSONArray) responseAPI.get(NiFiConnectorUtils.FIELD_USERGROUPS); // extracts the list of user groups
		if (groups == null) // there are no user groups yet
			groups = new JSONArray();
		return groups;
	}
	
	/**
	 * Given the username, finds the JSONObject representing such user.
	 * 
	 * @param userToFind - Username
	 * @return - JSONObject representing the input user
	 */
	private JSONObject getUser(String userToFind) {
		if (userToFind == null || userToFind.equals(""))
			return null;
		JSONArray users = listUsers(); // lists all users
		if (users == null) // no users found
			return null;
		JSONObject user, component;
		String userName;
		for (Object u : users) { // cycles through all existing users to find the one with the input name
			user = (JSONObject) u;
			component = (JSONObject) user.get(NiFiConnectorUtils.FIELD_COMPONENT);
			userName = component.getAsString(NiFiConnectorUtils.FIELD_IDENTITY);
			if (userName.equals(userToFind)) // user found
				return user; // returns the user
		}
		return null; // user not found
	}
	
	/**
	 * Lists all users.
	 * 
	 * @return - Array of all users
	 */
	private JSONArray listUsers() {
		String errMessage = "API call to list users failed";
		JSONObject responseAPI = callAPI(NiFiConnectorUtils.METHOD_GET, NiFiConnectorUtils.listUsersUrl(), null, errMessage);
		
		JSONArray users = (JSONArray) responseAPI.get(NiFiConnectorUtils.FIELD_USERS);// extracts the list of users
		if (users == null) // there are no users yet
			users = new JSONArray();
		return users;
	}
	
	/**
	 * Lists owners of an organization.
	 * 
	 * @param organization - Organization to list owners of
	 * @return - List of owners of the organization
	 */
	private List<String> listOwners(String organization) {
		if (organization == null || organization.equals(""))
			return null;
		List<String> owners = new ArrayList<String>();
		JSONObject group = getUserGroup(organization + ":" + NiFiConnectorUtils.getOwnerRole(), listUserGroups());
		JSONObject groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
		JSONArray users = (JSONArray) groupComponent.get(NiFiConnectorUtils.FIELD_USERS); // members of the user group
		
		JSONObject userInGroup, userInGroupComponent;
		for (Object u : users) { // searches for the right user to remove
			userInGroup = (JSONObject) u;
			userInGroupComponent = (JSONObject) userInGroup.get(NiFiConnectorUtils.FIELD_COMPONENT);
			owners.add(userInGroupComponent.getAsString(NiFiConnectorUtils.FIELD_IDENTITY));
		}
		return owners;
	}
	
	/**
	 * Adds an owner to the input organization.
	 * 
	 * @param ownerInfo - New owner
	 * @param organizationName - Name of the organization
	 */
	public String addOwner(UserInfo ownerInfo, String organizationName) {
		JSONObject owner = getUser(ownerInfo.getUsername());
		if (owner == null) // cannot find user
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Unable to add owner; user " + ownerInfo.getUsername() + " could not be found.");
		JSONObject organizationPG = getProcessGroup(organizationName, NiFiConnectorUtils.ROOT);
		if (organizationPG == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Unable to add owner; process group for organization " + organizationName + " could not be found.");
		
		String organizationPGID = organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the organization's process group
		JSONArray nestedPGs = listProcessGroups(organizationPGID); // lists all process groups nested within the organization's process group
		JSONObject group, groupComponent;
		String groupName;
		for (Object o : nestedPGs) { // new owner is given ownership for all tenants of the organization
			group = (JSONObject) o;
			groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
			groupName = groupComponent.getAsString(NiFiConnectorUtils.FIELD_NAME);
			assignRoleToUser(groupName + ":" + NiFiConnectorUtils.getOwnerRole(), organizationName, ownerInfo);
		}
		// New owner is given ownership of the organization's process group
		assignRoleToUser(organizationName + ":" + NiFiConnectorUtils.getOwnerRole(), null, ownerInfo);
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, ownerInfo.getUsername() + " has been registered as owner of " + organizationName + ".");
	}
	
	/**
	 * Removes an owner from an organization.
	 * 
	 * @param ownerInfo - Owner to remove
	 * @param organizationName - Name of the organization
	 */
	public String removeOwner(UserInfo ownerInfo, String organizationName) {
		JSONObject user = getUser(ownerInfo.getUsername());
		if (user == null) // cannot find user
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Unable to remove owner; user " + ownerInfo.getUsername() + " could not be found.");
		JSONObject organizationPG = getProcessGroup(organizationName, NiFiConnectorUtils.ROOT);
		if (organizationPG == null)
			return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 2, "Unable to remove owner; process group for organization " + organizationName + " could not be found.");
		
		String organizationPGID = organizationPG.getAsString(NiFiConnectorUtils.FIELD_ID); // ID of the organization's process group
		JSONArray nestedPGs = listProcessGroups(organizationPGID); // lists all process groups nested within the organization's process group
		JSONObject group, groupComponent;
		String groupName;
		for (Object o : nestedPGs) { // ownership for all tenants of the organization is revoked from the user
			group = (JSONObject) o;
			groupComponent = (JSONObject) group.get(NiFiConnectorUtils.FIELD_COMPONENT);
			groupName = groupComponent.getAsString(NiFiConnectorUtils.FIELD_NAME);
			revokeRoleFromUser(groupName + ":" + NiFiConnectorUtils.getOwnerRole(), organizationName, ownerInfo);
		}
		// Former owner is revoked ownership of the organization's process group
		revokeRoleFromUser(organizationName + ":" + NiFiConnectorUtils.getOwnerRole(), null, ownerInfo);
		return CommonUtils.formatResult(NiFiConnectorUtils.getComponentId(), 0, ownerInfo.getUsername() + " is no longer owner of " + organizationName + ".");
	}
	
	/**
	 * Given an input JSONObject, builds a JSONObject from it made to be an element in a sub-array.
	 * 
	 * @param element - The element to take revision and id fields from
	 * @return - A JSONObject ready to be added to a sub-array
	 */
	private static JSONObject provideElementForSubArray(JSONObject element) {
		JSONObject newElement = new JSONObject(); // only revision and ID are required
		newElement.appendField(NiFiConnectorUtils.FIELD_REVISION, element.get(NiFiConnectorUtils.FIELD_REVISION));
		newElement.appendField(NiFiConnectorUtils.FIELD_ID, element.get(NiFiConnectorUtils.FIELD_ID));
		return newElement;
	}
	
	/**
	 * Returns the array of user groups that have the specified permission on the input process group.
	 * 
	 * @param processGroupName - Process group
	 * @param organizationName - Name of the organization the process group belongs to
	 * @param action - read/write
	 * @param userGroups - Array of all user groups
	 * @return - Array of user groups that can execute action on processGroup
	 */
	private static JSONArray provideUserGroupsForPolicy(String processGroupName, String organizationName, String action, JSONArray userGroups) {
		JSONArray newUserGroups = new JSONArray(); // the array of user groups with this permission
		JSONObject newUserGroup, userGroup, userGroupComponent;
		String userGroupName, userGroupRole;
		
		JSONObject revision = new JSONObject(); // revision is required when creating new objects
		revision.appendField(NiFiConnectorUtils.FIELD_VERSION, 0);
		
		String nameToCompare = processGroupName;
		if (organizationName != null)
			nameToCompare = organizationName + "/" + nameToCompare;
		
		for (Object g : userGroups) { // loop on all existing user groups
			userGroup = (JSONObject) g;
			userGroupComponent = (JSONObject) userGroup.get(NiFiConnectorUtils.FIELD_COMPONENT);
			userGroupName = userGroupComponent.getAsString(NiFiConnectorUtils.FIELD_IDENTITY); // name of the user group
			userGroupRole = userGroupName.substring(userGroupName.lastIndexOf(":") + 1); // role on the tenant
			
			if (userGroupName.substring(0, userGroupName.lastIndexOf(":")).equals(nameToCompare)) { // user group found, add its ID
				// All roles associated to this process group may read, but only specific roles may write
				if (action.equals(NiFiConnectorUtils.ACTION_READ) || (action.equals(NiFiConnectorUtils.ACTION_WRITE) && NiFiConnectorUtils.hasWritePrivilege(userGroupRole)) ) {
					newUserGroup = new JSONObject(); // only revision and ID are required
					newUserGroup.appendField(NiFiConnectorUtils.FIELD_REVISION, revision);
					newUserGroup.appendField(NiFiConnectorUtils.FIELD_ID, userGroup.getAsString(NiFiConnectorUtils.FIELD_ID));
					newUserGroups.add(newUserGroup); // adds user group
				}
			}
		}
		
		return newUserGroups;
	}
	
	/**
	 * Determines the coordinates for a new process group.
	 * 
	 * @param component - Coordinates are to be appended to this JSONObject
	 */
	private static void setPosition(JSONObject component) {
		JSONObject position = new JSONObject();
		// Generates a random position, otherwise NiFi would just stack all new process groups at (0,0).
		// The new process group may still overlap with a different one, but even with an algorithm to find
		// a proper empty space, the position may not be ideal
		Random r = new Random();
		position.appendField(NiFiConnectorUtils.FIELD_X, r.nextDouble()*2000); // random X
		position.appendField(NiFiConnectorUtils.FIELD_Y, r.nextDouble()*2000); // random Y
		component.appendField("position", position);
	}
	
	/**
	 * Updates the user group identified by the input ID.
	 * 
	 * @param groupId - ID of the user group to update
	 * @param groupName - Name of the user group to update, only used for error messages
	 * @param group - JSONObject containing the changes
	 * @return - Updated user group
	 */
	private JSONObject updateUserGroup(String groupId, String groupName, JSONObject group) {
		String groupMessage = groupName != null ? groupName : groupId; // if the group's name is defined, use it in the error message, otherwise use its ID
		String errMessage = "API call to update group " + groupMessage + " failed";
		return callAPI(NiFiConnectorUtils.METHOD_PUT, NiFiConnectorUtils.updateUserGroupUrl(groupId), group, errMessage);
	}
	
	/**
	 * Executes a call to one of NiFi's APIs.
	 * 
	 * @param method - GET, POST, PUT, DELETE
	 * @param url - API endpoint
	 * @param body - Only for POST and PUT requests
	 * @param errMessage - Message to give in case of failure
	 * @return - Content of the response, as JSONObject
	 */
	private JSONObject callAPI(String method, String url, JSONObject body, String errMessage) {
		try {
			// Sets HTTP request to use admin's certificate
			HttpClient client = HttpClients.custom().setSSLContext(getSSLContext()).build();
			RequestBuilder builder = RequestBuilder.create(method);
			builder.setUri(url); // endpoint
			if (body != null) { // only for POST and PUT
				StringEntity entity = new StringEntity(body.toJSONString());
				entity.setContentType("application/json");
				builder.setEntity(entity);
			}
			HttpUriRequest request = builder.build();
			HttpResponse response = client.execute(request);
			if (!NiFiConnectorUtils.acceptedSC(response.getStatusLine().getStatusCode())) // checks status code
				throw new HttpException(errMessage + "; status code: " + response.getStatusLine().getStatusCode());
			JSONParser jsonParser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
			return (JSONObject) jsonParser.parse(EntityUtils.toString(response.getEntity())); // parses result as JSONObject
		} catch (IOException | HttpException | ParseException | net.minidev.json.parser.ParseException e) {
			throw new ComponentException(NiFiConnectorUtils.getComponentId() + ": error while performing " + method + " on endpoint " + url + ": " + e);
		}
	}
	
	/**
	 * Sets up key store and trust store in a SSLContext to be used for authenticated HTTP requests.
	 * 
	 * @return - The SSLContext, ready to be used in HTTP requests
	 */
	private SSLContext getSSLContext() {
		try {
			KeyStore keyStore = KeyStore.getInstance(NiFiConnectorUtils.getKeystoreType()); // retrieves the admin's certificate
			keyStore.load(new FileInputStream(NiFiConnectorUtils.getKeyStorePath()), NiFiConnectorUtils.getKeystoreExportPassword().toCharArray());
			KeyStore trustStore = KeyStore.getInstance(NiFiConnectorUtils.getTruststoreType()); // retrieves NiFi's truststore
			trustStore.load(new FileInputStream(NiFiConnectorUtils.getTruststorePath()), NiFiConnectorUtils.getTruststorePassword().toCharArray());
			return SSLContexts.custom().loadTrustMaterial(trustStore, null).loadKeyMaterial(keyStore, NiFiConnectorUtils.getKeystoreExportPassword().toCharArray()).build();
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | KeyManagementException e) {
			throw new ComponentException(NiFiConnectorUtils.getComponentId() + ": error while retrieving NiFi administrator's certificate: " + e);
		}
	}
	
}
