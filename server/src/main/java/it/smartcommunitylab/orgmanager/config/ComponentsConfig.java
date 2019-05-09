package it.smartcommunitylab.orgmanager.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.InvalidConfigurationException;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;
import it.smartcommunitylab.orgmanager.dto.ComponentsModel;

/**
 * Reads configuration for the components from the application-components.yml file.
 */
@Configuration
@EnableConfigurationProperties
public class ComponentsConfig {
	
	@Autowired
	private ComponentsConfiguration componentsConfiguration;
	
	private final Log log = LogFactory.getLog(ComponentsConfig.class);
	
	// Static class used to load components configuration
	public static class ComponentsConfiguration {
		private List<Map<String, String>> components;
		
		public List<Map<String, String>> getComponents() {
			return components;
		}
		
		public void setComponents(List<Map<String, String>> components) {
			this.components = components;
		}
	}
	
	/**
	 *  Reads components configuration from YAML file
	 * @return
	 */
	@Bean
	@ConfigurationProperties("componentsconfig")
	public ComponentsConfiguration getComponentsConfiguration() {
		return new ComponentsConfiguration();
	}
	/**
	 * Creates a map that links component ID to the connector for such component.
	 * 
	 * @return Map: component ID -> connector for the component
	 */
	@Bean
	public ComponentsModel getComponentsMap() {
		ComponentsModel componentMap = new ComponentsModel();
		String missingComponentIdErr = "The application-components.yml configuration file is invalid: one of the components is missing the following required property: ";
		String missingFieldErr = "The application-components.yml configuration file is invalid: component %s is missing the following required property: %s";
		String componentId, implementation, roles, format;
		for (Map<String, String> map : componentsConfiguration.getComponents()) {
			componentId = map.get(Constants.FIELD_COMPONENT_ID);
			if (componentId == null || componentId.equals("")) // componentId property is required
				throw new InvalidConfigurationException(missingComponentIdErr + Constants.FIELD_COMPONENT_ID);
			implementation = map.get(Constants.FIELD_IMPLEMENTATION);
			if (implementation == null || implementation.equals("")) // implementation property is required
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, Constants.FIELD_IMPLEMENTATION));
			roles = map.get(Constants.FIELD_ROLES);
			if (roles == null || roles.equals("")) // roles property is required
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, Constants.FIELD_ROLES));
			format = map.get(Constants.FIELD_FORMAT);
			if (format == null || format.equals("")) // when missing, format is given a default value
				map.put(Constants.FIELD_FORMAT, Constants.DEFAULT_FORMAT);
			Class customClass;
			try {
				customClass = Class.forName(implementation);
				Component component = (Component) customClass.newInstance(); // instantiates the component with a subclass
				component.init(map); // initializes the component, passing its properties to it
				componentMap.getListComponents().put(componentId, component); // adds the component to the map
			} catch (ClassNotFoundException e) {
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, Constants.FIELD_IMPLEMENTATION));
			} catch (InstantiationException e) {
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, Constants.FIELD_IMPLEMENTATION));
			} catch (IllegalAccessException e) {
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, Constants.FIELD_IMPLEMENTATION));
			}
		}
		
		return componentMap;
	}
	
}