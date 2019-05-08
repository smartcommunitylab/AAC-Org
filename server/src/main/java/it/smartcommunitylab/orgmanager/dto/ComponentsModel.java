package it.smartcommunitylab.orgmanager.dto;

import java.util.HashMap;
import java.util.Map;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;

public class ComponentsModel {

	private Map<String,Component> listComponents = new HashMap<String,Component>();

	public Map<String, Component> getListComponents() {
		return listComponents;
	}

	public void setListComponents(Map<String, Component> listComponents) {
		this.listComponents = listComponents;
	}
	
	
}
