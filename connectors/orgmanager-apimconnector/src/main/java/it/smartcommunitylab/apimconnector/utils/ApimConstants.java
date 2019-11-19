package it.smartcommunitylab.apimconnector.utils;

public class ApimConstants {

	public static final String USER_MGT_ENDPOINT 	= "usermgmtEndpoint";
	public static final String USER_MGT_PASSWORD 	= "usermgmtPassword";
	public static final String TENANT_MGT_ENDPOINT 	= "multitenancyEndpoint";
	public static final String TENANT_MGT_PASSWORD 	= "multitenancyPassword";
	public static final String COMPONENT_ID 		= "componentId";
	public static final String HOST 				= "host";
	
	public static final String INTERNAL_PUBLISHER = "Internal/publisher";
	public static final String INTERNAL_SUBSCRIBER = "Internal/subscriber";
	
	private static final String[] SUBSCRIBER = {INTERNAL_SUBSCRIBER}; //,ROLE_IDENTITY};
	
	public static String[] subscriberRoles() {
		return SUBSCRIBER;
	}

	
}
