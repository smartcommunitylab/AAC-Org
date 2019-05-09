package it.smartcommunitylab.apimconnector;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.um.ws.api.stub.ClaimValue;
import org.wso2.carbon.um.ws.api.stub.RemoteUserStoreManagerServiceUserStoreExceptionException;
import it.smartcommunitylab.aac.wso2.model.RoleModel;
import it.smartcommunitylab.apimconnector.services.TenantManagementService;
import it.smartcommunitylab.apimconnector.services.UserManagementService;
import it.smartcommunitylab.apimconnector.utils.APIMConnectorUtils;
import it.smartcommunitylab.apimconnector.utils.ApimConstants;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.componentsmodel.UserInfo;
import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonConstants;

@Service("it.smartcommunitylab.apimconnector.APIMConnector")
public class APIMConnector implements Component{

	private UserManagementService umService;
	private TenantManagementService tmService;
	private Log log = LogFactory.getLog(APIMConnector.class);
	
	public void init(Map<String, String> properties) {
		APIMConnectorUtils.init(properties);
		tmService = new TenantManagementService(APIMConnectorUtils.getMultitenancyEndpoint(), APIMConnectorUtils.getMultitenancyPassword());
		umService = new UserManagementService(APIMConnectorUtils.getUsermgmtEndpoint(), APIMConnectorUtils.getUsermgmtPassword(), tmService);
	}

	public String createOrganization(String organizationName, UserInfo owner) {
		return CommonConstants.SUCCESS_MSG;
	}

	public String deleteOrganization(String organizationName, List<String> tenants) {
		String message = CommonConstants.SUCCESS_MSG;
		try {
			for (String tenant : tenants) {
				deleteTenant(tenant, organizationName);
			}
		} catch(Exception e) {
			message = e.getMessage();
		}
		return message;
	}

	public String createUser(UserInfo user) {
		String message = CommonConstants.SUCCESS_MSG;
		String userName = user.getUsername();
		String password = new BigInteger(50, new SecureRandom()).toString(16);
		String [] roles = new String[] {};
		ClaimValue [] claims = new ClaimValue[] {};
		try {
			umService.createNormalUser(userName, password, roles, claims);
		} catch (AxisFault e) {
			message = e.getMessage();
		} catch (RemoteException e) {
			message = e.getMessage();
		} catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			message = e.getMessage();
		}
		return message;
	}

	public String removeUserFromOrganization(String userName, String organizationName) {
		return CommonConstants.SUCCESS_MSG;
	}

	public String assignRoleToUser(String role, String organization, String userName) {
		String message = CommonConstants.SUCCESS_MSG;
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setAddRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userName, organization);
		} catch (AxisFault e) {
			message = CommonConstants.SUCCESS_MSG;
		} catch (RemoteException e) {
			message = CommonConstants.SUCCESS_MSG;
		} catch (TenantMgtAdminServiceExceptionException e) {
			message = CommonConstants.SUCCESS_MSG;
		} catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			message = CommonConstants.SUCCESS_MSG;
		}
		return message;
	}

	public String revokeRoleFromUser(String role, String organization, String userName) {
		String message = CommonConstants.SUCCESS_MSG;
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setRemoveRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userName, organization);
		} catch (AxisFault e) {
			message = e.getMessage();
		} catch (RemoteException e) {
			message = e.getMessage();
		} catch (TenantMgtAdminServiceExceptionException e) {
			message = e.getMessage();
		} catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			message = e.getMessage();
		}
		return message;
	}

	public String addOwner(String ownerName, String organizationName) {
		return CommonConstants.SUCCESS_MSG;
	}

	public String removeOwner(String ownerName, String organizationName) {
		return CommonConstants.SUCCESS_MSG;
	}

	public String createTenant(String tenant, String organization, UserInfo ownerInfo) {
		String message = CommonConstants.SUCCESS_MSG;
		try {
			String password = new BigInteger(50, new SecureRandom()).toString(16);
			tmService.createTenant(tenant, ownerInfo.getUsername(), password, ownerInfo.getName(), ownerInfo.getSurname()); 
		} catch (AxisFault e) {
			message = e.getMessage();
		} catch (RemoteException e) {
			message = e.getMessage();
		} catch (TenantMgtAdminServiceExceptionException e) {
			message = e.getMessage();
		}
		return message;
	}

	public String deleteTenant(String tenant, String organization) {
		String message = CommonConstants.SUCCESS_MSG;
		try {
			tmService.deleteTenant(tenant);
		} catch (AxisFault e) {
			message = e.getMessage();
		} catch (RemoteException e) {
			message = e.getMessage();
		} catch (TenantMgtAdminServiceExceptionException e) {
			message = e.getMessage();
		}
		return message;
	}

	public void activateTenant(String nameTenant) {
		TenantInfoBean bean = null;
		try {
			bean = tmService.getTenant(nameTenant);
			bean.setActive(true);
			tmService.updateTenant(bean);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TenantMgtAdminServiceExceptionException e) {
			e.printStackTrace();
		}
	}
}
