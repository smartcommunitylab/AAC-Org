package it.smartcommunitylab.orgmanager.common;

/**
 * 
 * The list of constants that are going to be used globally
 * 
 */
public class Constants {
	
	public static final String ROLE_PROVIDER 					= "ROLE_PROVIDER";
	public static final String ROLE_MEMBER						= "ROLE_USER";
	public static final String ROOT_COMPONENTS 					= "components";
	public static final String ROOT_ORGANIZATIONS 				= "organizations";
	public static final String ROOT_RESOURCES	 				= "resources";
	
	public static final String PATH_COMPONENTS_CONFIG 			= "/config/components.yml";
	public static final String SCOPE_MANAGE_ROLES	 			= "profile.basicprofile.all,user.roles.write,user.roles.read,user.roles.read.all,client.roles.read.all,user.roles.manage.all";
	public static final String SCOPE_ORG_MANAGEMENT             = "orgmanagement";
	
	//ComponentConfig
	public static final String FIELD_NAME 						= "name";
	public static final String FIELD_COMPONENT_ID 				= "componentId";
	public static final String FIELD_ROLES 						= "roles";
	public static final String SLUG_PATTERN                     = "^[a-z0-9_.\\-]*$";
	public static final String NAME_PATTERN                     = "^[a-zA-Z0-9 _-]+$";

}
