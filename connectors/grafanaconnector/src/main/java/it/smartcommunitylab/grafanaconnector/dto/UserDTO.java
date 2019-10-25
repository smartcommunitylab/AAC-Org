package it.smartcommunitylab.grafanaconnector.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class UserDTO {

	private String id;
	private String name;
	private String login;
	private String password;
	private String email;
	private String theme;
	private String orgId;
	private boolean isAdmin;
	private boolean isGrafanaAdmin;
	private boolean isDisabled;
	private boolean isExternal;
	private Set<?> authLabels = new HashSet<>(); 
	private Date updatedAt;
	private Date createdAt;
	
	
	public UserDTO(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public boolean getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public boolean isGrafanaAdmin() {
		return isGrafanaAdmin;
	}

	public void setGrafanaAdmin(boolean isGrafanaAdmin) {
		this.isGrafanaAdmin = isGrafanaAdmin;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public Set<?> getAuthLabels() {
		return authLabels;
	}

	public void setAuthLabels(Set<?> authLabels) {
		this.authLabels = authLabels;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
