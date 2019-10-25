package it.smartcommunitylab.grafanaconnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import it.smartcommunitylab.grafanaconnector.dto.OrganizationDTO;
import it.smartcommunitylab.grafanaconnector.dto.UserDTO;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;

@Service("it.smartcommunitylab.grafanaconnector.GrafanaConnector")
public class GrafanaConnector implements Component {

	private static final Log log = LogFactory.getLog(GrafanaConnector.class);
	
	@Override
	public String init(Map<String, String> properties) {
		GrafanaConnectorUtils.initializeProperties(properties);
		return null;
	}

	@Override
	public String createOrganization(String organizationName, UserInfo owner) {
		GrafanaConnectorUtils.logInfo("Starting to create Organization in Grafana", log);
		try {
			String url 		= GrafanaConnectorUtils.getCrudOrganizationURL();
			OrganizationDTO organizationDTO = new OrganizationDTO(organizationName);
			Map<String, String> response = GrafanaConnectorUtils.requestPOSTApi(url, organizationDTO, HttpMethod.POST);
			GrafanaConnectorUtils.logInfo("Ending successfully the creation of new Organization in Grafana", log);
			return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "Organization has been successfully created. " + response.get("message"));
		} catch(Exception e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the Organization creation: " + e.getMessage());
			GrafanaConnectorUtils.logError(error, log);
			return error;
		}
	}

	@Override
	public String deleteOrganization(String organizationName, List<String> tenants) {
		GrafanaConnectorUtils.logInfo("Starting to delete Organization in Grafana", log);
		try {
			String url 		= GrafanaConnectorUtils.getCrudOrganizationURL();
			// get the proper organization id belonging to the provided organization name
			String orgId 	= retrieveOrganizationId(organizationName); 
			if(orgId != null) {
				url += "/"+orgId;
				Map<String, String> response = GrafanaConnectorUtils.requestDELETEApi(url);
				GrafanaConnectorUtils.logInfo("Ending successfully the deletion of Organization name: " + organizationName + ", id: " + orgId + " in Grafana", log);
				return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "Organization has been successfully deleted. " + response.get("message"));
			} else {
				return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Not able to find the organization");
			}
		} catch (GrafanaException e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the Organization deletion process: " + e.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
			return error;
		}
	}

	@Override
	public String createUser(UserInfo user) {
		GrafanaConnectorUtils.logInfo("Starting to create global User in Grafana", log);
		try {
			String url 		= GrafanaConnectorUtils.getCrudAdminUserURL();
			String password = GrafanaConnectorUtils.getUserStaticPassword();
			UserDTO userDto = new UserDTO(user.getName(), user.getUsername(), password);
			Map<String, String> response = GrafanaConnectorUtils.requestPOSTApi(url, userDto, HttpMethod.POST);
			GrafanaConnectorUtils.logInfo("Ending successfully the creation of new User in Grafana", log);
			return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "User successfully created: " + response.get("message"));
		} catch (Exception e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the User creation: " + e.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
			return error;
		}
	}

	@Override
	public String removeUserFromOrganization(UserInfo user, String organizationName, List<String> tenants) {
		GrafanaConnectorUtils.logInfo("Starting to remove User form Oranization in Grafana", log);
		try {
			String url = GrafanaConnectorUtils.getCrudOrganizationURL();
			String orgId 	= retrieveOrganizationId(organizationName);
			String userId = retrieveUserId(user.getUsername());
			url += "/"+orgId+"/users/"+userId;
			Map<String, String> response = GrafanaConnectorUtils.requestDELETEApi(url);
			GrafanaConnectorUtils.logInfo("Ending successfully the process of removing User from Organization in Grafana", log);
			return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "User successfully removed: " + response.get("message"));
		} catch (Exception e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during removeUserFromOrganization: " + e.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
			return error;
		}
	}

	/**
	 * This method will update the user role that belong to specific organization
	 */
	@Override
	public String assignRoleToUser(String fullRole, String organization, UserInfo user) {
		GrafanaConnectorUtils.logInfo("Starting the method assignRoleToUser for orgName: " + organization + ",fullRole: " + fullRole + ", username: " + user.getUsername(), log);
		try {
			// Determines the role to assign
			String role = fullRole.substring(fullRole.indexOf(":") + 1);
			if(role.equals("ROLE_PROVIDER"))
				role = GrafanaConnectorContants.GRAFANA_ADMIN;
			else
				role = GrafanaConnectorContants.GRAFANA_VIEWER;
			GrafanaConnectorUtils.logInfo("The role to be assigned to this user is: " + role, log);
			String url = GrafanaConnectorUtils.getCrudOrganizationURL();
			String orgId 	= retrieveOrganizationId(organization);
			String userId = retrieveUserId(user.getUsername());
			url += "/"+orgId+"/users/"+userId;
		
			Map<String, String> parameters = new HashMap<>();
			parameters.put("role", role);
			Map<String, String> response = GrafanaConnectorUtils.requestPOSTApi(url, parameters, HttpMethod.PATCH);
			GrafanaConnectorUtils.logInfo("Ending successfully the process of Assigning Role to User from Organization " + organization + "in Grafana", log);
			return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "User successfully updated: " + response.get("message"));
		} catch (GrafanaException e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the assignRoleToUser process: " + e.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
			return error;
		}
	}

	@Override
	public String revokeRoleFromUser(String role, String organization, UserInfo user) {
		return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 1, "Revoke Role is not supported, instead this is managed by the role assignment method");
	}

	/**
	 * In this method it will add the existing user to the proper organization
	 * and grant him Admin role
	 */
	@Override
	public String addOwner(UserInfo owner, String organizationName) {
		GrafanaConnectorUtils.logInfo("Starting the method addOwner for orgName: " + organizationName + ", username: " + owner.getUsername(), log);
		addUserInOrganization(owner, organizationName, GrafanaConnectorContants.GRAFANA_ADMIN);
		GrafanaConnectorUtils.logInfo("Ending successfully the process addOwner " + organizationName + "in Grafana", log);
		return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "User has successfully been converted to owner" );	
	}

	/**
	 * In this method it will add the existing user to the proper organization
	 * and grant him Viewer role
	 */
	@Override
	public String removeOwner(UserInfo owner, String organizationName) {
		GrafanaConnectorUtils.logInfo("Starting the method removeOwner for orgName: " + organizationName + ", username: " + owner.getUsername(), log);
		addUserInOrganization(owner, organizationName, GrafanaConnectorContants.GRAFANA_VIEWER);
		GrafanaConnectorUtils.logInfo("Ending successfully the process removeOwner " + organizationName + "in Grafana", log);
		return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "User has successfully been converted to viewer");
	}

	@Override
	public String createTenant(String tenant, String organization, UserInfo userInfo) {
		return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 1, "Tenant provisioning not supported");
	}

	@Override
	public String updateTenant(String tenant, String organization) {
		return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 1, "Tenant provisioning not supported");
	}

	@Override
	public String deleteTenant(String tenant, String organization) {
		return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 1, "Tenant provisioning not supported");
	}
	
	/**
	 * Method to retrieve the Organization Id belonging to the organization name
	 * @return
	 * @throws GrafanaException 
	 */
	private String retrieveOrganizationId(String organizationName) throws GrafanaException{
		GrafanaConnectorUtils.logInfo("Starting the method retrieveOrganizationId for orgName: " + organizationName, log);
		String orgId = null;
		Map<String,Object> organizationData = new HashMap<>();
		String url = GrafanaConnectorUtils.getCrudOrganizationURL();
		url += "/name/" + organizationName;
		try {
			organizationData = GrafanaConnectorUtils.requestGETApi(url);
			for(String key : organizationData.keySet()) {
				if(key.equals("id")) {
					orgId = Integer.toString((int)organizationData.get("id"));
				}
			}
		} catch (Exception rex) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the getOrganizationId process: " + rex.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
            throw new GrafanaException(error + rex.getMessage());
        }
		GrafanaConnectorUtils.logInfo("Ending the method retrieveOrganizationId for orgName: " + organizationName, log);
		return orgId;
	}
	
	/**
	 * Method to retrieve the User Id belonging to the username 
	 * @param username
	 * @return
	 */
	private String retrieveUserId(String username) {
		GrafanaConnectorUtils.logInfo("Starting the method retrieveUserId for username: " + username, log);
		String userId = null;
		Map<String,Object> userData = new HashMap<>();
		String url = GrafanaConnectorUtils.getCrudUserURL();
		url += "/lookup?loginOrEmail=" + username;
		try {
			userData = GrafanaConnectorUtils.requestGETApi(url);
			for(String key : userData.keySet()) {
				if(key.equals("id")) {
					userId = Integer.toString((int)userData.get("id"));
				}
			}
		} catch (GrafanaException e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the retrieveUserId process for user: " + username + e.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
		}
		GrafanaConnectorUtils.logInfo("Ending the method retrieveUserId for username: " + username, log);

		return userId;
	}
	
	private String addUserInOrganization(UserInfo owner, String organizationName, String role) {
		GrafanaConnectorUtils.logInfo("Starting the method addUserInOrganization for orgName: " + organizationName + ", username: " + owner.getUsername() + ", role: " + role, log);
		try {
			String url = GrafanaConnectorUtils.getCrudOrganizationURL();
			String orgId 	= retrieveOrganizationId(organizationName);
			url += "/"+orgId+"/users";
		
			Map<String, String> parameters = new HashMap<>();
			parameters.put("loginOrEmail", owner.getUsername());
			parameters.put("role", role);
			Map<String, String> response = GrafanaConnectorUtils.requestPOSTApi(url, parameters, HttpMethod.POST);
			GrafanaConnectorUtils.logInfo("Ending successfully the process addUserInOrganization " + organizationName + "in Grafana", log);
			return CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 0, "User successfully changed : " + response.get("message"));
		} catch (GrafanaException e) {
			String error = CommonUtils.formatResult(GrafanaConnectorUtils.getComponentId(), 2, "Something went wrong during the addUserInOrganization process: " + e.getMessage());
			GrafanaConnectorUtils.logInfo(error, log);
			return error;
		}	
	}
}
