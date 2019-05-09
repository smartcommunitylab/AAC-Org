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

	public void createUser(UserInfo user) { /* does nothing */ }
	
	public void removeUserFromOrganization(UserInfo userInfo, String organizationName) { /* does nothing */ }

	public void init(Map<String, String> properties) { /* does nothing */ }
	
	public void createOrganization(String organizationName, UserInfo owner) { /* does nothing */ }
	
	public void deleteOrganization(String organizationName, List<String> tenants) { /* does nothing */ }

	public void assignRoleToUser(String role, String organization, UserInfo userInfo) { /* does nothing */ }

	public void revokeRoleFromUser(String role, String organization, UserInfo userInfo) { /* does nothing */ }

	public void addOwner(UserInfo ownerInfo, String organizationName) { /* does nothing */ }

	public void removeOwner(UserInfo ownerInfo, String organizationName) { /* does nothing */ }

	public void createTenant(String tenant, String organization, UserInfo ownerInfo) { /* does nothing */ }

	public void deleteTenant(String tenant, String organization) { /* does nothing */ }

}
