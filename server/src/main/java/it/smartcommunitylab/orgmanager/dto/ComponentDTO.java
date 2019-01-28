package it.smartcommunitylab.orgmanager.dto;

import java.util.List;

public class ComponentDTO {
	private String name;
	private String componentId;
	private String scope;
	private String format;
	private String implementation;
	private List<String> roles;
	
	public ComponentDTO() {}
	
	public ComponentDTO(String name, String componentId, String scope, String format, String implementation, List<String> roles) {
		this.name = name;
		this.componentId = componentId;
		this.scope = scope;
		this.format = format;
		this.implementation = implementation;
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
	
	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public String getFormat() {
		return format;
	}
	
	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getImplementation() {
		return implementation;
	}
	
	public void setImplementation(String implementation) {
		this.implementation = implementation;
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
