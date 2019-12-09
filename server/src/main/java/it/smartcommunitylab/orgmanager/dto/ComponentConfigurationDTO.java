package it.smartcommunitylab.orgmanager.dto;

public class ComponentConfigurationDTO {
	private String componentId; // identifies the component this configuration is for
	
	public ComponentConfigurationDTO() {}
	
	public ComponentConfigurationDTO(String componentId) {
		this.componentId = componentId;
	}
	
	public String getComponentId() {
		return componentId;
	}
	
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	
}
