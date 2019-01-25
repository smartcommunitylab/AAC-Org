package it.smartcommunitylab.orgmanager.dto;

import java.util.HashSet;

public class ComponentConfigurationDTO {
	private String componentId;
	private HashSet<String> tenants;
	
	public ComponentConfigurationDTO() {}
	
	public ComponentConfigurationDTO(String componentId, HashSet<String> tenants) {
		this.componentId = componentId;
		this.tenants = tenants;
	}
	
	public String getComponentId() {
		return componentId;
	}
	
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	
	public HashSet<String> getTenants() {
		return tenants;
	}
	
	public void setTenants(HashSet<String> tenants) {
		this.tenants = tenants;
	}
	
	/**
	 * Adds a tenant to the list of tenants.
	 * 
	 * @param tenant - The tenant to add
	 */
	public void addTenant(String tenant) {
		if (tenants == null)
			tenants = new HashSet<String>();
		tenants.add(tenant);
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + " ID=" + componentId;
	}
}
