package it.smartcommunitylab.orgmanager.componentsmodel;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * This class is a dummy implementation of the Component interface and none of its methods do anything.
 * It is meant to be used for components that do not need any additional operation when one of the
 * Organization Management Console's APIs is called. Components may also be configured to use this
 * implementation to disable them.
 */
@Service("it.smartcommunitylab.orgmanager.componentsmodel.DefaultComponentImpl")
public class DefaultComponentImpl implements Component {

	public String createUser(UserInfo user) { return null; }
	
	public String removeUserFromOrganization(UserInfo userInfo, String organizationName, List<String> tenants) { return null; }

	public String init(Map<String, String> properties) { return null; }
	
	public String createOrganization(String organizationName, UserInfo owner) { return null; }
	
	public String deleteOrganization(String organizationName, List<String> tenants) { return null; }

	public String assignRoleToUser(String role, String organization, UserInfo userInfo) { return null; }

	public String revokeRoleFromUser(String role, String organization, UserInfo userInfo) { return null; }

	public String addOwner(UserInfo ownerInfo, String organizationName) { return null; }

	public String removeOwner(UserInfo ownerInfo, String organizationName) { return null; }

	public String createTenant(String tenant, String organization, UserInfo ownerInfo) { return null; }

	public String deleteTenant(String tenant, String organization) { return null; }

}
