package it.smartcommunitylab.apimconnector.services;

import it.smartcommunitylab.aac.wso2.WSO2Constans;
import it.smartcommunitylab.aac.wso2.model.RoleModel;
import it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceStub;
import it.smartcommunitylab.apim.user.stub.CustomUserStoreManagerServiceUserStoreExceptionException;
import it.smartcommunitylab.apimconnector.utils.Utils;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.tenant.mgt.stub.TenantMgtAdminServiceExceptionException;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.carbon.user.mgt.common.xsd.ClaimValue;
import org.wso2.carbon.utils.CarbonUtils;

public class UserManagementService {

	private String umEndpoint;
	private String umPassword;
	
	private TenantManagementService tenantService;
	private CustomUserStoreManagerServiceStub umStub;

	private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;
	
	public UserManagementService(String umEndpoint, String umPassword, TenantManagementService tenantService) {
		this.umEndpoint = umEndpoint;
		this.umPassword = umPassword;
		this.tenantService = tenantService;
	}
	
	protected CustomUserStoreManagerServiceStub getUMStub() throws AxisFault {
		if (umStub == null) {
			umStub = new CustomUserStoreManagerServiceStub(null, umEndpoint);
			CarbonUtils.setBasicAccessSecurityHeaders("admin", umPassword, true, umStub._getServiceClient());
			ServiceClient client = umStub._getServiceClient();
			Options options = client.getOptions();
			options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
			options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
			options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
			options.setCallTransportCleanup(true);
			options.setManageSession(true);
			Utils.disableSSLValidator(client);
		}
		return umStub;
	}
	
	/**
	 * Create new user
	 * @param userName
	 * @param password
	 * @param roles
	 * @param claims
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 */
	public void createNormalUser(String userName, String password, String[] roles, ClaimValue[] claims, int tenantId, String tenantDomain) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		getUMStub().addUser(userName, password, roles, claims, null, false, tenantId, tenantDomain);
	}

	/**
	 * Update user password
	 * @param userName
	 * @param password
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 */
	public void updateNormalUserPassword(String userName, String password) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		getUMStub().updateCredentialByAdmin(userName, password);
	}

	/**
	 * Update publisher password
	 * @param userName
	 * @param domain
	 * @param password
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 * @throws TenantMgtAdminServiceExceptionException 
	 */
	public void updatePublisherPassword(String userName, String domain, String password) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException, TenantMgtAdminServiceExceptionException {
		TenantInfoBean bean = tenantService.getTenant(domain);
		bean.setAdminPassword(password);
		tenantService.updateTenant(bean);
	}

	/**
	 * Create WSO2 API Publisher/creator
	 * @param userName
	 * @param password
	 * @param claims
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 * @throws TenantMgtAdminServiceExceptionException 
	 */
	public void createPublisher(String domain, String userName, String password, String firstName, String lastName) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException, TenantMgtAdminServiceExceptionException {
		tenantService.createTenant(domain, userName, password, firstName, lastName);
	}
	
	/**
	 * Create WSO2 API Subscriber
	 * @param userName
	 * @param password
	 * @param claims
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 */
	public void createSubscriber(String userName, String password, ClaimValue[] claims) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		getUMStub().addUser(userName, password, WSO2Constans.subscriberRoles(), claims, null, false, 3, "today.com");
	}	
	
	/**
	 * Delete user
	 * @param userName
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 */
	public void deleteNormalUser(String userName) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		getUMStub().deleteUser(userName);
	}
	
	/**
	 * 
	 * @param userName
	 * @return
	 * @throws AxisFault
	 * @throws RemoteException
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
	 */
	public boolean checkNormalUserExists(String userName) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		return getUMStub().isExistingUser(userName);
	}
	
	/**
	 * @param userName
	 * @param domain the user belongs to
	 * @return List of user roles
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException 
	 * @throws RemoteException 
	 * @throws AxisFault 
	 */
	public List<String> getUserRoles(String userName, String domain) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		return Arrays.asList(getUMStub().getRoleListOfUser(Utils.getUserNameAtTenant(userName, domain)));
	}

	/**
	 * @param userName
	 * @return List of user roles
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException 
	 * @throws RemoteException 
	 * @throws AxisFault 
	 * @throws CustomUserStoreManagerServiceUserStoreExceptionException 
	 */
	public List<String> getNormalUserRoles(String userName) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		return Arrays.asList(getUMStub().getRoleListOfUser(userName));
	}
	
	public boolean isUserInRole(String username, String role, String roleDomain) throws AxisFault, RemoteException, TenantMgtAdminServiceExceptionException, CustomUserStoreManagerServiceUserStoreExceptionException {
		int tenantId = tenantService.getTenant(roleDomain).getTenantId();
		String[] users = getUMStub().getUserListOfRole(role+"@"+tenantId);
		if (users != null) {
			for (String user : users) {
				if (user.equals(username)) return true;
			}
		}
		return false;
	}
	
	/**
	 * Update user roles from the specified role model
	 * 
	 * @param roleModel
	 * @param username
	 * @param domain
	 * @throws TenantMgtAdminServiceExceptionException 
	 * @throws RemoteException 
	 * @throws AxisFault 
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException 
	 */
	public void updateRoles(RoleModel roleModel, String username, int tenantId, String domain) throws AxisFault, RemoteException, TenantMgtAdminServiceExceptionException, CustomUserStoreManagerServiceUserStoreExceptionException {
		String[] toDel = {}, toAdd = {};
		if (roleModel.getAddRoles() != null && roleModel.getAddRoles().size() > 0) {
			toAdd = new String[roleModel.getAddRoles().size()];
			for (int i = 0; i < toAdd.length; i++) toAdd[i] = fullName(roleModel.getAddRoles().get(i), tenantId);
		}
		if (roleModel.getRemoveRoles() != null && roleModel.getRemoveRoles().size() > 0) {
			toDel = new String[roleModel.getRemoveRoles().size()];
			for (int i = 0; i < toDel.length; i++) toDel[i] = fullName(roleModel.getRemoveRoles().get(i), tenantId);
		}
		if (toAdd != null || toDel != null) {
			getUMStub().updateRoleListOfUser(username, toDel, toAdd, tenantId, domain);
		} 
	}


//	/**
//	 * Create a new role in a specified domain
//	 * @param roleName
//	 * @param domain
//	 * @throws AxisFault
//	 * @throws RemoteException
//	 * @throws TenantMgtAdminServiceExceptionException
//	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException
//	 */
//	public void createRole(String roleName, String domain) throws AxisFault, RemoteException, TenantMgtAdminServiceExceptionException, RemoteUserStoreManagerServiceUserStoreExceptionException {
//		int tenantId = tenantService.getTenant(domain).getTenantId();
//		getUMStub().addRole(roleName+"@"+tenantId, null, null);
//	}
//	
//	/**
//	 * Delete a specified role in a domain
//	 * @param testRole
//	 * @param testDomain
//	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException 
//	 * @throws RemoteException 
//	 * @throws AxisFault 
//	 * @throws TenantMgtAdminServiceExceptionException 
//	 */
//	public void deleteRole(String roleName, String domain) throws AxisFault, RemoteException, RemoteUserStoreManagerServiceUserStoreExceptionException, TenantMgtAdminServiceExceptionException {
//		int tenantId = tenantService.getTenant(domain).getTenantId();
//		getUMStub().deleteRole(roleName+"@"+tenantId);
//	}

	/**
	 * @param username
	 * @param password
	 * @throws RemoteUserStoreManagerServiceUserStoreExceptionException 
	 * @throws RemoteException 
	 * @throws AxisFault 
	 */
	public boolean authenticate(String username, String password) throws AxisFault, RemoteException, CustomUserStoreManagerServiceUserStoreExceptionException {
		boolean authenticate = getUMStub().authenticate(username, password);
		return authenticate;
	}

	/**
	 * @param string
	 * @param tenantId
	 * @return
	 */
	private String fullName(String string, int tenantId) {
		// TOD check correctness of shared role semantics
		if (tenantId <= 0) return string;
		
		String suffix = "@"+tenantId;
		if (string.endsWith(suffix)) return string;
		return string + suffix;
	}
}