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

@Service("it.smartcommunitylab.apimconnector.ApiMConnector")
public class APIMConnector implements Component{

	private UserManagementService umService;
	private TenantManagementService tmService;
	
	public void init(Map<String, String> properties) {
		APIMConnectorUtils.init(properties);
		tmService = new TenantManagementService(APIMConnectorUtils.getMultitenancyEndpoint(), APIMConnectorUtils.getMultitenancyPassword());
		umService = new UserManagementService(APIMConnectorUtils.getUsermgmtEndpoint(), APIMConnectorUtils.getUsermgmtPassword(), tmService);
	}

	public void createOrganization(String organizationName, UserInfo owner) {
		// TODO Auto-generated method stub
		
	}

	public void deleteOrganization(String organizationName, List<String> tenants) {
		for (String tenant : tenants) {
			deleteTenant(tenant, organizationName);
		}
	}

	public void createUser(UserInfo user) {
		String userName = user.getUsername();
		String password = new BigInteger(50, new SecureRandom()).toString(16);
		String [] roles = new String[] {};
		ClaimValue [] claims = new ClaimValue[] {};
		try {
			umService.createNormalUser(userName, password, roles, claims);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			e.printStackTrace();
		}
	}

	public void removeUserFromOrganization(String userName, String organizationName) {
		// TODO Auto-generated method stub
		
	}

	public void assignRoleToUser(String role, String organization, String userName) {
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setAddRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userName, organization);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TenantMgtAdminServiceExceptionException e) {
			e.printStackTrace();
		} catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			e.printStackTrace();
		}
	}

	public void revokeRoleFromUser(String role, String organization, String userName) {
		List<String> rolesList = Arrays.asList(new String[]{role});
		RoleModel roleModel = new RoleModel();
		roleModel.setRemoveRoles(rolesList);
		try {
			umService.updateRoles(roleModel, userName, organization);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TenantMgtAdminServiceExceptionException e) {
			e.printStackTrace();
		} catch (RemoteUserStoreManagerServiceUserStoreExceptionException e) {
			e.printStackTrace();
		}
	}

	public void addOwner(String ownerName, String organizationName) {
		// TODO Auto-generated method stub
		
	}

	public void removeOwner(String ownerName, String organizationName) {
		// TODO Auto-generated method stub
		
	}

	public void createTenant(String tenant, String organization, UserInfo ownerInfo) {
		try {
			//TODO extend the owner info details in the general interface
			String password = new BigInteger(50, new SecureRandom()).toString(16);
			tmService.createTenant(tenant, ownerInfo.getUsername(), password, ownerInfo.getName(), ownerInfo.getSurname()); 
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TenantMgtAdminServiceExceptionException e) {
			e.printStackTrace();
		}
	}

	public void deleteTenant(String tenant, String organization) {
		try {
			tmService.deleteTenant(tenant);
		} catch (AxisFault e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (TenantMgtAdminServiceExceptionException e) {
			e.printStackTrace();
		}
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
