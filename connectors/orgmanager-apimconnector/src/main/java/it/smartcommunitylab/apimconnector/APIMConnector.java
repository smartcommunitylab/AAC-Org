package it.smartcommunitylab.apimconnector;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.axis2.AxisFault;
import org.springframework.stereotype.Service;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import it.smartcommunitylab.aac.wso2.model.RoleModel;
import it.smartcommunitylab.apimconnector.services.TenantManagementService;
import it.smartcommunitylab.apimconnector.services.UserManagementService;
import it.smartcommunitylab.apimconnector.utils.APIMConnectorUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;

@Service("it.smartcommunitylab.apimconnector.APIMConnector")
public class APIMConnector implements Component{

	private UserManagementService umService;
	private TenantManagementService tmService;
	
	public String init(Map<String, String> properties) {
		APIMConnectorUtils.init(properties);
		tmService = new TenantManagementService(APIMConnectorUtils.getMultitenancyEndpoint(), APIMConnectorUtils.getMultitenancyPassword());
		umService = new UserManagementService(APIMConnectorUtils.getUsermgmtEndpoint(), APIMConnectorUtils.getUsermgmtPassword(), tmService);
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Initializaton complete.");
	}

	public String createOrganization(String organizationName, UserInfo owner) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "Organization creation does not apply to this component.");
	}

	public String deleteOrganization(String organizationName, List<String> tenants) {
		for (String tenant : tenants)
			deleteTenant(tenant, organizationName);
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "All tenants of organization " + organizationName + " have been deleted.");
	}

	public String createUser(UserInfo userInfo) {
		String password = new BigInteger(50, new SecureRandom()).toString(16);
		String [] roles = new String[] {};
		ClaimValue [] claims = new ClaimValue[] {};
		try {
			umService.createNormalUser(userInfo.getUsername(), password, roles, claims);
		} catch (RemoteException | RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while creating user " + userInfo + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "User " + userInfo + " has been created.");
	}

	public String removeUserFromOrganization(UserInfo userInfo, String organizationName, List<String> tenants) {
		try {
			List<String> rolesList = umService.getNormalUserRoles(userInfo.getUsername());
			RoleModel roleModel = new RoleModel();
			roleModel.setRemoveRoles(rolesList);
			for (String tenant : tenants)
				umService.updateRoles(roleModel,  userInfo.getUsername(), tenant);
		} catch (RemoteException | RemoteUserStoreManagerServiceUserStoreExceptionException | TenantMgtAdminServiceExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while removing user " + userInfo + " from organization " + organizationName + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "User " + userInfo + " has been removed from all tenants of organization " + organizationName + ".");
	}

	public String assignRoleToUser(String fullRole, String organization, UserInfo userInfo) {
		String domain = fullRole.substring(0, fullRole.indexOf(":"));
		String role = fullRole.substring(fullRole.indexOf(":") + 1);
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setAddRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userInfo.getUsername(), domain);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException | RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while assigning role " + role + " to " + userInfo + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Role " + role + " has been assigned to user " + userInfo + ".");
	}

	public String revokeRoleFromUser(String fullRole, String organization, UserInfo userInfo) {
		String domain = fullRole.substring(0, fullRole.indexOf(":"));
		String role = fullRole.substring(fullRole.indexOf(":") + 1);
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setRemoveRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userInfo.getUsername(), domain);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException | RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while revoking role " + role + " from " + userInfo + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Role " + role + " has been revoked from user " + userInfo + ".");
	}

	public String addOwner(UserInfo ownerInfo, String organizationName) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "Adding owners does not apply to this component.");
	}

	public String removeOwner(UserInfo ownerInfo, String organizationName) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "Removing owners does not apply to this component.");
	}

	public String createTenant(String tenant, String organization, UserInfo ownerInfo) {
		String password = new BigInteger(50, new SecureRandom()).toString(16);
			try {
				tmService.createTenant(tenant, ownerInfo.getUsername(), password, ownerInfo.getName(), ownerInfo.getSurname());
			} catch (AxisFault e) {
				return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while deleting tenant " + tenant + ": " + e.getMessage());
			} catch (RemoteException e) {
				return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while deleting tenant " + tenant + ": " + e.getMessage());
			} catch (TenantMgtAdminServiceExceptionException e) {
				return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while deleting tenant " + tenant + ": " + e.getMessage());
			}
		
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Tenant " + tenant + " has been created with owner " + ownerInfo + ".");
	}

	public String deleteTenant(String tenant, String organization) {
		try {
			tmService.deleteTenant(tenant);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while deleting tenant " + tenant + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Tenant " + tenant + " has been deleted.");
	}

	public void activateTenant(String tenant) {
		TenantInfoBean bean = null;
		try {
			bean = tmService.getTenant(tenant);
			bean.setActive(true);
			tmService.updateTenant(bean);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while activating tenant " + tenant + ": " + e.getMessage());
		}
	}
	
}
