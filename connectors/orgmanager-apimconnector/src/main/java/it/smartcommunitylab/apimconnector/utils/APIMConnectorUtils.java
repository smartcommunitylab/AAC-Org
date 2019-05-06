package it.smartcommunitylab.apimconnector.utils;

import java.util.Map;
import it.smartcommunitylab.orgmanager.componentsmodel.ComponentException;

public class APIMConnectorUtils {

	private static String componentId;
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
		usermgmtEndpoint 		= properties.get(ApimConstants.USER_MGT_ENDPOINT);
		usermgmtPassword 		= properties.get(ApimConstants.USER_MGT_ENDPOINT);
		multitenancyEndpoint 	= properties.get(ApimConstants.TENANT_MGT_ENDPOINT);
		multitenancyPassword 	= properties.get(ApimConstants.TENANT_MGT_PASSWORD);

	}

	public static String getComponentId() {
		return componentId;
	}

	public static String getUsermgmtEndpoint() {
		return usermgmtEndpoint;
	}

	public static String getUsermgmtPassword() {
		return usermgmtPassword;
	}

	public static String getMultitenancyEndpoint() {
		return multitenancyEndpoint;
	}

	public static String getMultitenancyPassword() {
		return multitenancyPassword;
	}

}
