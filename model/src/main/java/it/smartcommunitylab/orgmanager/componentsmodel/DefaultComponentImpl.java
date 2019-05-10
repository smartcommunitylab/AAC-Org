package it.smartcommunitylab.orgmanager.componentsmodel;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import it.smartcommunitylab.orgmanager.componentsmodel.utils.CommonConstants;

/**
 * This class is a dummy implementation of the Component interface and none of its methods do anything.
 * It is meant to be used for components that do not need any additional operation when one of the
 * Organization Management Console's APIs is called. Components may also be configured to use this
 * implementation to disable them.
 */
@Service("it.smartcommunitylab.orgmanager.componentsmodel.DefaultComponentImpl")
public class DefaultComponentImpl implements Component {

	public String createUser(UserInfo user) { return CommonConstants.SUCCESS_MSG; }
	
	public String removeUserFromOrganization(UserInfo userInfo, String organizationName, List<String> tenants) { return CommonConstants.SUCCESS_MSG; }

	public String init(Map<String, String> properties) { return CommonConstants.SUCCESS_MSG; }
	
	public String createOrganization(String organizationName, UserInfo owner) { return CommonConstants.SUCCESS_MSG; }
	
	public String deleteOrganization(String organizationName, List<String> tenants) { return CommonConstants.SUCCESS_MSG; }

	public String assignRoleToUser(String role, String organization, UserInfo userInfo) { return CommonConstants.SUCCESS_MSG; }

	public String revokeRoleFromUser(String role, String organization, UserInfo userInfo) { return CommonConstants.SUCCESS_MSG; }

	public String addOwner(UserInfo ownerInfo, String organizationName) { return CommonConstants.SUCCESS_MSG; }

	public String removeOwner(UserInfo ownerInfo, String organizationName) { return CommonConstants.SUCCESS_MSG; }

	public String createTenant(String tenant, String organization, UserInfo ownerInfo) { return CommonConstants.SUCCESS_MSG; }

	public String deleteTenant(String tenant, String organization) { return CommonConstants.SUCCESS_MSG; }

}
