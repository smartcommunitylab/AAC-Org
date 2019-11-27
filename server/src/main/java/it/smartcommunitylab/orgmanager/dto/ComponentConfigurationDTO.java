package it.smartcommunitylab.orgmanager.dto;

import java.util.HashSet;
import java.util.Set;

public class ComponentConfigurationDTO {
	private String componentId; // identifies the component this configuration is for
	private Set<String> tenants;
	
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
	
	public Set<String> getTenants() {
		return tenants;
	}
	
	public void setTenants(Set<String> tenants) {
		this.tenants = tenants;
	}
	
	/**
	 * Adds a tenant to the tenants set.
	 * 
	 * @param tenant - The tenant to add
	 */
	public void addTenant(String tenant) {
		if (tenants == null)
			tenants = new HashSet<String>(); // initializes the set
		tenants.add(tenant); // adds the tenant
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + " ID=" + componentId;
	}
}
