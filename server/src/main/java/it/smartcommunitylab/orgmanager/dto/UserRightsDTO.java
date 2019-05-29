package it.smartcommunitylab.orgmanager.dto;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

public class UserRightsDTO {
	private String userName;
	private boolean admin;
	private Collection<Long> ownedOrganizations;
	
	public UserRightsDTO(String userName, boolean admin, Collection<Long> ownedOrganizations) {
		this.userName = userName;
		this.admin = admin;
		this.ownedOrganizations = ownedOrganizations;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public boolean getAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public Collection<Long> getOwnedOrganizations() {
		return ownedOrganizations;
	}
	
	public void setOwnedOrganizations(Collection<Long> ownedOrganizations) {
		this.ownedOrganizations = ownedOrganizations;
	}
	
	@Override
	public String toString() {
		return "[" + userName + " authorizations] Admin: " + admin + ", Owned organizations: [" + StringUtils.join(ownedOrganizations, ',') + "]";
	}
}
