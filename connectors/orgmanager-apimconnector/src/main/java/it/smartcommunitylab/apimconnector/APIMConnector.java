package it.smartcommunitylab.apimconnector;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.axis2.AxisFault;
import org.springframework.stereotype.Service;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.user.mgt.common.xsd.ClaimValue;

import it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;
import it.smartcommunitylab.apimconnector.model.RoleModel;
import it.smartcommunitylab.apimconnector.services.LoginAdminService;
import it.smartcommunitylab.apimconnector.services.TenantManagementService;
import it.smartcommunitylab.apimconnector.services.UserManagementService;
import it.smartcommunitylab.apimconnector.utils.APIMConnectorUtils;
import it.smartcommunitylab.apimconnector.utils.ApimConstants;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonUtils;

@Service("it.smartcommunitylab.apimconnector.APIMConnector")
public class APIMConnector implements Component{

	private UserManagementService umService;
	private TenantManagementService tmService;
	private LoginAdminService loginService;
	
	@Override
	public String init(Map<String, String> properties) {
		APIMConnectorUtils.init(properties);
		tmService = new TenantManagementService(APIMConnectorUtils.getMultitenancyEndpoint(), APIMConnectorUtils.getMultitenancyPassword());
		umService = new UserManagementService(APIMConnectorUtils.getUsermgmtEndpoint(), APIMConnectorUtils.getUsermgmtPassword(), tmService);
		loginService = new LoginAdminService(APIMConnectorUtils.getHost());
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Initializaton complete.");
	}

	@Override
	public String createOrganization(String organizationName, UserInfo owner) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "Organization creation does not apply to this component.");
	}

	@Override
	public String deleteOrganization(String organizationName, List<String> tenants) {
		for (String tenant : tenants)
			deleteTenant(tenant, organizationName);
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "All tenants of organization " + organizationName + " have been deleted.");
	}

	@Override
	public String createUser(UserInfo userInfo) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "User creation is handled when a role is assigned.");
	}

	@Override
	public String removeUserFromOrganization(UserInfo userInfo, String organizationName, List<String> tenants) {
		try {
			List<String> rolesList = new ArrayList<String>();
			rolesList.add(ApimConstants.INTERNAL_PUBLISHER);
			rolesList.add(ApimConstants.INTERNAL_SUBSCRIBER);
			RoleModel roleModel = new RoleModel();
			roleModel.setRemoveRoles(rolesList);
			for (String tenantDomain : tenants) {
				int tenantId = tmService.getTenant(tenantDomain).getTenantId();
				umService.updateRoles(roleModel,  userInfo.getUsername(), tenantId, tenantDomain);
			}
		} catch (RemoteException | CustomUserStoreManagerServiceUserStoreExceptionException | TenantMgtAdminServiceExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while removing user " + userInfo + " from organization " + organizationName + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "User " + userInfo + " has been removed from all tenants of organization " + organizationName + ".");
	}

	@Override
	public String assignRoleToUser(String fullRole, String organization, UserInfo userInfo) {
		// Determines the domain
		String domain = fullRole.substring(0, fullRole.indexOf(":"));
		int tenantId;
		try {
			TenantInfoBean tenant = tmService.getTenant(domain);
			if (tenant == null)
				throw new TenantMgtAdminServiceExceptionException("tenant does not exist.");
			tenantId = tenant.getTenantId();
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while retrieving tenant " + domain + ": " + e.getMessage());
		}
		
		// Determines the role to assign
		String role = fullRole.substring(fullRole.indexOf(":") + 1);
		if(role.equals("ROLE_PUBLISHER"))
			role = ApimConstants.INTERNAL_PUBLISHER;
		else
			role = ApimConstants.INTERNAL_SUBSCRIBER;
		
		// User creation
		String password = new BigInteger(50, new SecureRandom()).toString(16);
		String [] roles = new String[] {};
		ClaimValue [] claims = new ClaimValue[] {};
		try {
			umService.createNormalUser(userInfo.getUsername(), password, roles, claims, tenantId, domain);
		} catch (AxisFault e) {
			// User already exists; no action
		} catch (RemoteException | CustomUserStoreManagerServiceUserStoreExceptionException e) {
			// Something went wrong while creating the user
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while creating user " + userInfo + ": " + e.getMessage());
		}
		
		// Assigns the role
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setAddRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userInfo.getUsername(), tenantId, domain);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException | CustomUserStoreManagerServiceUserStoreExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while assigning role " + role + " to " + userInfo + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Role " + role + " has been assigned to user " + userInfo + ".");
	}

	@Override
	public String revokeRoleFromUser(String fullRole, String organization, UserInfo userInfo) {
		String domain = fullRole.substring(0, fullRole.indexOf(":"));
		String role = fullRole.substring(fullRole.indexOf(":") + 1);
		if(role.equals("ROLE_PUBLISHER"))
			role = ApimConstants.INTERNAL_PUBLISHER;
		else 
			role = ApimConstants.INTERNAL_SUBSCRIBER;
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setRemoveRoles(rolesList);
		try {
			int tenantId = tmService.getTenant(domain).getTenantId();
			umService.updateRoles(roleModel, userInfo.getUsername(), tenantId, domain);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException | CustomUserStoreManagerServiceUserStoreExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, ": error while revoking role " + role + " from " + userInfo + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Role " + role + " has been revoked from user " + userInfo + ".");
	}

	@Override
	public String addOwner(UserInfo ownerInfo, String organizationName) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "Adding owners does not apply to this component.");
	}

	@Override
	public String removeOwner(UserInfo ownerInfo, String organizationName) {
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 1, "Removing owners does not apply to this component.");
	}

	@Override
	public String createTenant(String tenant, String organization, UserInfo ownerInfo) {
		String password = new BigInteger(50, new SecureRandom()).toString(16);
			try {
				tmService.createTenant(tenant, ownerInfo.getUsername(), password, ownerInfo.getName(), ownerInfo.getSurname());
				loginService.authenticate(ownerInfo.getUsername()+"@"+tenant, password);
			} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
				return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while creating tenant " + tenant + ": " + e.getMessage());
			}
		
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Tenant " + tenant + " has been created with owner " + ownerInfo + ".");
	}

	@Override
	public String deleteTenant(String tenant, String organization) {
		try {
			tmService.deleteTenant(tenant);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while deleting tenant " + tenant + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Tenant " + tenant + " has been deleted.");
	}

	@Override
	public String updateTenant(String tenant, String organization) {
		TenantInfoBean bean = null;
		try {
			bean = tmService.getTenant(tenant);
			bean.setActive(true);
			tmService.updateTenant(bean);
		} catch (RemoteException | TenantMgtAdminServiceExceptionException e) {
			CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 2, "error while activating tenant " + tenant + ": " + e.getMessage());
		}
		return CommonUtils.formatResult(APIMConnectorUtils.getComponentId(), 0, "Tenant " + tenant + " has been updated.");
	}
	
}
