package it.smartcommunitylab.orgmanager.dto;

import java.util.List;

public class ComponentDTO {
	private String name;
	private String componentId; // identifies the component
	private List<String> roles; // roles that may be assigned within the component
	
	public ComponentDTO() {}
	
	public ComponentDTO(String name, String componentId, List<String> roles) {
		this.name = name;
		this.componentId = componentId;
		this.roles = roles;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getComponentId() {
		return componentId;
	}
	
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	
	public List<String> getRoles() {
		return roles;
	}
	
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [" + componentId + "]: Name=" + name;
	}
}
