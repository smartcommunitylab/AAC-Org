package it.smartcommunitylab.orgmanager.componentsmodel;

import java.util.List;
import java.util.Map;

public interface Component {
	
	/**
	 * Performs initialization operations, using input properties.
	 * 
	 * @param properties - Properties to use during initialization
	 */
	public void init(Map<String, String> properties);
	
	/**
	 * Handles creation of an organization.
	 * 
	 * @param organizationName - Name of the organization
	 * @param owner - Owner of the organization
	 */
	public void createOrganization(String organizationName, UserInfo owner);
	
	/**
	 * Handles deletion of an organization.
	 * 
	 * @param organizationName - Name of the organization
	 * @param tenants - Tenants belonging to the organization
	 */
	public void deleteOrganization(String organizationName, List<String> tenants);
	
	/**
	 * Creates a user
	 * @param user - User to create
	 */
	public void createUser(UserInfo user);
	
	/**
	 * Removes a user from an organization.
	 * 
	 * @param userName - Name of the user
	 * @param organizationName - Name of the organization
	 */
	public void removeUserFromOrganization(String userName, String organizationName);
	
	/**
	 * Assigns a role to the user.
	 * 
	 * @param role - Role to assign
	 * @param organization - Organization
	 * @param userName - User to give the role to
	 */
	public void assignRoleToUser(String role, String organization, String userName);
	
	/**
	 * Revokes a role from the user.
	 * 
	 * @param role - Role to revoke
	 * @param organization - Organization
	 * @param userName - User to revoke the role from
	 */
	public void revokeRoleFromUser(String role, String organization, String userName);
	
	/**
	 * Adds an owner to the organization.
	 * 
	 * @param ownerName - Name of the owner
	 * @param organizationName - Name of the organization
	 */
	public void addOwner(String ownerName, String organizationName);
	
	/**
	 * Removes an owner from the organization.
	 * 
	 * @param ownerName - Name of the owner
	 * @param organizationName - Name of the organization
	 */
	public void removeOwner(String ownerName, String organizationName);
	
	/**
	 * Creates a tenant for the given organization.
	 * 
	 * @param tenant - Tenant to create
	 * @param organization - Name of the organization
	 * @param userInfo - Owner of the organization
	 */
	public void createTenant(String tenant, String organization, UserInfo userInfo);
	
	/**
	 * Removes a tenant from the given organization.
	 * 
	 * @param tenant - Tenant to delete
	 * @param organization - Name of the organization
	 */
	public void deleteTenant(String tenant, String organization);
}
