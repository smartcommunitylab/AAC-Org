package it.smartcommunitylab.orgmanager.dto;

public class ComponentConfigurationDTO {
	private String componentId; // identifies the component this configuration is for
	private String name;
	
	public ComponentConfigurationDTO() {}
	
	public ComponentConfigurationDTO(String componentId, String name) {
		this.componentId = componentId;
		this.name = name;
	}
	
	public String getComponentId() {
		return componentId;
	}
	
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
