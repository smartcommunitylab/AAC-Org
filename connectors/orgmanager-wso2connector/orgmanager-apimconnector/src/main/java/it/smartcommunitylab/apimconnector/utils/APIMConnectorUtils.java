package it.smartcommunitylab.apimconnector.utils;

import java.util.Map;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.ServerConstants;

import it.smartcommunitylab.orgmanager.componentsmodel.ComponentException;

public class APIMConnectorUtils {

	private static String componentId;
	private static String host;
	private static String usermgmtEndpoint;
	private static String usermgmtPassword;
	private static String multitenancyEndpoint;
	private static String multitenancyPassword;
	
	/**
	 * Initializes various fields.
	 * 
	 * @param properties - Map: name of property -> value of property
	 */
	public static void init(Map<String, String> properties) {
		if (properties == null)
			throw new ComponentException("No properties specified.");
		
		componentId 			= properties.get(ApimConstants.COMPONENT_ID);
		host 					= properties.get(ApimConstants.HOST);
		usermgmtEndpoint 		= properties.get(ApimConstants.USER_MGT_ENDPOINT);
		usermgmtPassword 		= properties.get(ApimConstants.USER_MGT_PASSWORD);
		multitenancyEndpoint 	= properties.get(ApimConstants.TENANT_MGT_ENDPOINT);
		multitenancyPassword 	= properties.get(ApimConstants.TENANT_MGT_PASSWORD);

	}

	public static String getComponentId() {
		return componentId;
	}

	public static String getUsermgmtEndpoint() {
		return host + usermgmtEndpoint;
	}

	public static String getUsermgmtPassword() {
		return usermgmtPassword;
	}

	public static String getMultitenancyEndpoint() {
		return host + multitenancyEndpoint;
	}

	public static String getMultitenancyPassword() {
		return multitenancyPassword;
	}
	
	public static String getHost() {
		return host;
	}

	public static String startTenantFlow(String tenantDomain, int tenantId) {
        if (tenantId == -1) {
            return "Invalid Tenant Domain: " + tenantDomain;
        }
        if ( !tenantDomain.equals("carbon.super")) {
        	PrivilegedCarbonContext.startTenantFlow();
        	PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        } 
        return tenantDomain;
    }

    public static void endTenantFlow(){
    	PrivilegedCarbonContext.endTenantFlow();
    }
}
