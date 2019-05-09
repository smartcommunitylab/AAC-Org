package it.smartcommunitylab.orgmanager.componentsmodel;

import java.util.List;
import java.util.Map;

public interface Component {
	
	/**
	 * Performs initialization operations, using input properties.
	 * 
	 * @param properties - Properties to use during initialization
	 */
	public String init(Map<String, String> properties);
	
	/**
	 * Handles creation of an organization.
	 * 
	 * @param organizationName - Name of the organization
	 * @param owner - Owner of the organization
	 */
	public String createOrganization(String organizationName, UserInfo owner);
	
	/**
	 * Handles deletion of an organization.
	 * 
	 * @param organizationName - Name of the organization
	 * @param tenants - Tenants belonging to the organization
	 */
	public String deleteOrganization(String organizationName, List<String> tenants);
	
	/**
	 * Creates a user
	 * @param user - User to create
	 */
	public String createUser(UserInfo user);
	
	/**
	 * Removes a user from an organization.
	 * 
	 * @param user - User to remove
	 * @param organizationName - Name of the organization
	 * @param tenants - Tenants of the organization
	 */
	public String removeUserFromOrganization(UserInfo user, String organizationName, List<String> tenants);
	
	/**
	 * Assigns a role to the user.
	 * 
	 * @param role - Role to assign
	 * @param organization - Organization
	 * @param user - User to give the role to
	 */
	public String assignRoleToUser(String role, String organization, UserInfo user);
	
	/**
	 * Revokes a role from the user.
	 * 
	 * @param role - Role to revoke
	 * @param organization - Organization
	 * @param user - User to revoke the role from
	 */
	public String revokeRoleFromUser(String role, String organization, UserInfo user);
	
	/**
	 * Adds an owner to the organization.
	 * 
	 * @param owner - New owner
	 * @param organizationName - Name of the organization
	 */
	public String addOwner(UserInfo owner, String organizationName);
	
	/**
	 * Removes an owner from the organization.
	 * 
	 * @param owner - Owner to remove
	 * @param organizationName - Name of the organization
	 */
	public String removeOwner(UserInfo owner, String organizationName);
	
	/**
	 * Creates a tenant for the given organization.
	 * 
	 * @param tenant - Tenant to create
	 * @param organization - Name of the organization
	 * @param userInfo - Owner of the organization
	 */
	public String createTenant(String tenant, String organization, UserInfo userInfo);
	
	/**
	 * Removes a tenant from the given organization.
	 * 
	 * @param tenant - Tenant to delete
	 * @param organization - Name of the organization
	 */
	public String deleteTenant(String tenant, String organization);
}
