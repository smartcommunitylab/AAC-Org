package it.smartcommunitylab.nificonnector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.smartcommunitylab.orgmanager.componentsmodel.ComponentException;

public class NiFiConnectorUtils {
	public static final String ROOT = "root"; // root process group ID
	public static final String FLOW = "/flow"; // resource indicating the flow
	
	// Request methods
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";
	
	// Names of fields needed when building JSONObjects to submit to NiFi's APIs
	public static final String FIELD_ID = "id"; // identifies the object; used for users, user groups, process groups, policies...
	public static final String FIELD_VERSION = "version"; // version of the object
	public static final String FIELD_REVISION = "revision"; // contains the version
	public static final String FIELD_COMPONENT = "component"; // contains various other fields such as identity or name
	public static final String FIELD_IDENTITY = "identity"; // name of the user or user group
	public static final String FIELD_NAME = "name"; // name of the process group
	public static final String FIELD_PARENT_GROUP_ID = "parentGroupId"; // identifies the parent process group of a process group
	public static final String FIELD_USERS = "users"; // array used to list users
	public static final String FIELD_USERGROUPS = "userGroups"; // array used to list user groups
	public static final String FIELD_PROCESSGROUPS = "processGroups"; // array used to list process groups
	public static final String FIELD_ACTION = "action"; // its value is either read or write; used in policies
	public static final String FIELD_RESOURCE = "resource"; // identifies the resource a policy is applied to
	public static final String FIELD_X = "x"; // X coordinate of a process group
	public static final String FIELD_Y = "y"; // Y coordinate of a process group
	
	// Types of resource that a policy may be assigned to
	public static final String TYPE_PROCESS_GROUP = "/process-groups/"; // view/modify process group flow
	public static final String TYPE_OPERATION = "/operation/process-groups/"; // operate (run, stop, etc.) the process group
	public static final String TYPE_PROVENANCE = "/provenance-data/process-groups/"; // view provenance events
	public static final String TYPE_DATA = "/data/process-groups/"; // view/empty queues, view metadata, submit replays
	
	// Possible action values, used by policies
	public static final String ACTION_READ = "read";
	public static final String ACTION_WRITE = "write";
	
	// Names of properties to retrieve during initialization
	private static final String PROPERTY_COMPONENT_ID = "componentId";
	private static final String PROPERTY_HOST = "host";
	private static final String PROPERTY_LIST_USERS_API = "listUsersApi";
	private static final String PROPERTY_CREATE_USER_API = "createUserApi";
	private static final String PROPERTY_DELETE_USER_API = "deleteUserApi";
	private static final String PROPERTY_LIST_USER_GROUPS_API = "listUserGroupsApi";
	private static final String PROPERTY_CREATE_USER_GROUP_API = "createUserGroupApi";
	private static final String PROPERTY_UPDATE_USER_GROUP_API = "updateUserGroupApi";
	private static final String PROPERTY_DELETE_USER_GROUP_API = "deleteUserGroupApi";
	private static final String PROPERTY_GET_POLICY_API = "getPolicyApi";
	private static final String PROPERTY_CREATE_POLICY_API = "createPolicyApi";
	private static final String PROPERTY_UPDATE_POLICY_API = "updatePolicyApi";
	private static final String PROPERTY_LIST_PROCESS_GROUPS_API = "listProcessGroupsApi";
	private static final String PROPERTY_GET_PROCESS_GROUP_API = "getProcessGroupApi";
	private static final String PROPERTY_CREATE_PROCESS_GROUP_API = "createProcessGroupApi";
	private static final String PROPERTY_DELETE_PROCESS_GROUP_API = "deleteProcessGroupApi";
	
	private static final String PROPERTY_KEYSTORE_PATH = "keystorePath";
	private static final String PROPERTY_TRUSTSTORE_PATH = "truststorePath";
	private static final String PROPERTY_KEYSTORE_TYPE = "keystoreType";
	private static final String PROPERTY_TRUSTSTORE_TYPE = "truststoreType";
	private static final String PROPERTY_KEYSTORE_EXPORT_PASSWORD = "keystoreExportPassword";
	private static final String PROPERTY_TRUSTSTORE_PASSWORD = "truststorePassword";
	
	private static final String PROPERTY_ADMIN_NAME = "adminName";
	private static final String PROPERTY_OWNER_ROLE = "ownerRole";
	
	private static final String PROPERTY_READ_ROLES = "readRoles";
	private static final String PROPERTY_WRITE_ROLES = "writeRoles";
	
	// Properties
	private static String componentId; // component ID
	private static String host; // NiFi instance host
	private static String listUsersApi; // list all users
	private static String createUserApi; // create a user
	private static String deleteUserApi; // delete a user
	private static String listUserGroupsApi; // list all user groups
	private static String createUserGroupApi; // create a user group
	private static String updateUserGroupApi; // update a user group
	private static String deleteUserGroupApi; // deletes a user group
	private static String getPolicyApi; // retrieve a policy
	private static String createPolicyApi; // create a policy
	private static String updatePolicyApi; // update a policy
	private static String listProcessGroupsApi; // list all process groups under a parent process group
	private static String getProcessGroupApi; // retrieve a process group's info
	private static String createProcessGroupApi; // create a process group
	private static String deleteProcessGroupApi; // deletes a process group
	
	private static String keystorePath; // path to the keystore
	private static String truststorePath; // path to the truststore
	private static String keystoreType; // JKS, PKCS12...
	private static String truststoreType; // JKS, PKCS12...
	private static String keystoreExportPassword; // keystore export password
	private static String truststorePassword; // truststore password
	
	private static String adminName; // name of administrator user
	private static String ownerRole; // role assigned to the owner of an organization when the organization is created
	
	private static List<String> readRoles; // roles with only read permissions
	private static List<String> writeRoles; // roles with both read and write permissions
	
	/**
	 * Initializes various fields.
	 * 
	 * @param properties - Map: name of property -> value of property
	 */
	static void init(Map<String, String> properties) {
		if (properties == null)
			throw new ComponentException("No properties specified.");
		
		componentId = properties.get(PROPERTY_COMPONENT_ID);
		host = properties.get(PROPERTY_HOST);
		listUsersApi = properties.get(PROPERTY_LIST_USERS_API);
		createUserApi = properties.get(PROPERTY_CREATE_USER_API);
		deleteUserApi = properties.get(PROPERTY_DELETE_USER_API);
		listUserGroupsApi = properties.get(PROPERTY_LIST_USER_GROUPS_API);
		createUserGroupApi = properties.get(PROPERTY_CREATE_USER_GROUP_API);
		updateUserGroupApi = properties.get(PROPERTY_UPDATE_USER_GROUP_API);
		deleteUserGroupApi = properties.get(PROPERTY_DELETE_USER_GROUP_API);
		getPolicyApi = properties.get(PROPERTY_GET_POLICY_API);
		createPolicyApi = properties.get(PROPERTY_CREATE_POLICY_API);
		updatePolicyApi = properties.get(PROPERTY_UPDATE_POLICY_API);
		listProcessGroupsApi = properties.get(PROPERTY_LIST_PROCESS_GROUPS_API);
		getProcessGroupApi = properties.get(PROPERTY_GET_PROCESS_GROUP_API);
		createProcessGroupApi = properties.get(PROPERTY_CREATE_PROCESS_GROUP_API);
		deleteProcessGroupApi = properties.get(PROPERTY_DELETE_PROCESS_GROUP_API);
		
		keystorePath = properties.get(PROPERTY_KEYSTORE_PATH);
		truststorePath = properties.get(PROPERTY_TRUSTSTORE_PATH);
		keystoreType = properties.get(PROPERTY_KEYSTORE_TYPE);
		truststoreType = properties.get(PROPERTY_TRUSTSTORE_TYPE);
		keystoreExportPassword = properties.get(PROPERTY_KEYSTORE_EXPORT_PASSWORD);
		truststorePassword = properties.get(PROPERTY_TRUSTSTORE_PASSWORD);
		
		adminName = properties.get(PROPERTY_ADMIN_NAME);
		ownerRole = properties.get(PROPERTY_OWNER_ROLE);
		
		String readRolesString = properties.get(PROPERTY_READ_ROLES);
		String writeRolesString = properties.get(PROPERTY_WRITE_ROLES);
		readRoles = new ArrayList<String>();
		writeRoles = new ArrayList<String>();
		if (readRolesString != null) { // builds a list from the comma-separated string
			for (String s : readRolesString.split(",")) {
				if (!s.trim().equals(""))
					readRoles.add(s.trim());
			}
		}
		if (writeRolesString != null) { // builds a list from the comma-separated string
			for (String s : writeRolesString.split(",")) {
				if (!s.trim().equals(""))
					writeRoles.add(s.trim());
			}
		}
	}
	
	// Returns the component ID
	static String getComponentId() {
		checkValid(PROPERTY_COMPONENT_ID, componentId);
		return componentId;
	}
	
	// Each of the following methods creates a URL from the host and API end-point, sometimes using parameters to build it.
	
	public static String listUsersUrl() {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_LIST_USERS_API, listUsersApi);
		return host + listUsersApi; // use with GET
	}
	
	public static String createUserUrl() {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_CREATE_USER_API, createUserApi);
		return host + createUserApi; // use with POST
	}
	
	public static String deleteUserUrl(String id, String version) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_DELETE_USER_API, deleteUserApi);
		return host + deleteUserApi + id + "?version=" + version; // use with DELETE
	}
	
	public static String listUserGroupsUrl() {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_LIST_USER_GROUPS_API, listUserGroupsApi);
		return host + listUserGroupsApi; // use with GET
	}
	
	public static String createUserGroupUrl() {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_CREATE_USER_GROUP_API, createUserGroupApi);
		return host + createUserGroupApi; // use with POST
	}
	
	public static String updateUserGroupUrl(String id) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_UPDATE_USER_GROUP_API, updateUserGroupApi);
		return host + updateUserGroupApi + id; // use with PUT
	}
	
	public static String deleteUserGroupUrl(String id, String version) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_DELETE_USER_GROUP_API, deleteUserGroupApi);
		return host + deleteUserGroupApi + id + "?version=" + version; // use with DELETE
	}
	
	public static String getPolicyUrl(String action, String resource) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_GET_POLICY_API, getPolicyApi);
		return host + getPolicyApi + action + resource; // use with GET
	}
	
	public static String createPolicyUrl() {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_CREATE_POLICY_API, createPolicyApi);
		return host + createPolicyApi; // use with POST
	}
	
	public static String updatePolicyUrl(String id) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_UPDATE_POLICY_API, updatePolicyApi);
		return host + updatePolicyApi + id; // use with PUT
	}
	
	public static String listProcessGroupsUrl(String id) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_LIST_PROCESS_GROUPS_API, listProcessGroupsApi);
		return host + listProcessGroupsApi + id + "/process-groups"; // use with GET
	}
	
	public static String getProcessGroupUrl(String id) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_GET_PROCESS_GROUP_API, getProcessGroupApi);
		return host + getProcessGroupApi + id; // use with GET
	}
	
	public static String createProcessGroupUrl(String id) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_CREATE_PROCESS_GROUP_API, createProcessGroupApi);
		return host + createProcessGroupApi + id + "/process-groups"; // use with POST
	}
	
	public static String deleteProcessGroupUrl(String id, String version) {
		checkValid(PROPERTY_HOST, host);
		checkValid(PROPERTY_DELETE_PROCESS_GROUP_API, deleteProcessGroupApi);
		return host + deleteProcessGroupApi + id + "?version=" + version;// use with DELETE
	}
	
	// These methods return values necessary for retrieving the admin user's certificate
	public static String getKeyStorePath() {
		checkValid(PROPERTY_KEYSTORE_PATH, keystorePath);
		return keystorePath;
	}
	
	public static String getTruststorePath() {
		checkValid(PROPERTY_TRUSTSTORE_PATH, truststorePath);
		return truststorePath;
	}
	
	public static String getKeystoreType() {
		checkValid(PROPERTY_KEYSTORE_TYPE, keystoreType);
		return keystoreType;
	}
	
	public static String getTruststoreType() {
		checkValid(PROPERTY_TRUSTSTORE_TYPE, truststoreType);
		return truststoreType;
	}
	
	public static String getKeystoreExportPassword() {
		checkValid(PROPERTY_KEYSTORE_EXPORT_PASSWORD, keystoreExportPassword);
		return keystoreExportPassword;
	}
	
	public static String getTruststorePassword() {
		checkValid(PROPERTY_TRUSTSTORE_PASSWORD, truststorePassword);
		return truststorePassword;
	}
	
	// Returns the administrator user's name
	public static String getAdminName() {
		checkValid(PROPERTY_ADMIN_NAME, adminName);
		return adminName;
	}
	
	// Returns the role used by the identity provider to determine ownership
	public static String getOwnerRole() {
		checkValid(PROPERTY_OWNER_ROLE, ownerRole);
		return ownerRole;
	}
	
	/**
	 * Checks if the input property is valid.
	 * 
	 * @param name - Name of the property to check
	 * @param value - Value of the property to check
	 */
	private static void checkValid(String name, String value) {
		if (value == null || value.equals(""))
			throw new ComponentException(componentId + ": required property " + name + " has no value.");
	}
	
	/**
	 * Returns all possible roles that the identity provider may attribute to a user.
	 * 
	 * @return - Array of possible roles
	 */
	public static List<String> getPossibleRoles() {
		List<String> possibleRoles = new ArrayList<String>();
		possibleRoles.addAll(readRoles);
		possibleRoles.addAll(writeRoles);
		possibleRoles.add(ownerRole);
		return possibleRoles;
	}
	
	/**
	 * Returns true if the input role has write privilege, otherwise returns false
	 * @param role - Role to check
	 * @return - whether or not the input role has write privilege
	 */
	public static boolean hasWritePrivilege(String role) {
		if (ownerRole.equals(role) || writeRoles.contains(role))
			return true;
		return false;
	}
	
	/**
	 * Returns whether or not the response status code of a HTTPRequest was successful.
	 * 
	 * @param statusCode - Response status code
	 * @return
	 */
	public static boolean acceptedSC(int statusCode) {
		if (statusCode == 200 || statusCode == 201)
			return true;
		return false;
	}
}
