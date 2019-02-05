package it.smartcommunitylab.orgmanager.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import it.smartcommunitylab.orgmanager.common.InvalidConfigurationException;
import it.smartcommunitylab.orgmanager.common.OrgManagerUtils;
import it.smartcommunitylab.orgmanager.componentsmodel.Component;

/**
 * Reads configuration for the components from the application-components.yml file.
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class ComponentsConfig {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_COMPONENT_ID = "componentId";
	public static final String FIELD_SCOPE = "scope";
	public static final String FIELD_FORMAT = "format";
	public static final String FIELD_IMPLEMENTATION = "implementation";
	public static final String FIELD_ROLES = "roles";
	
	private static final String DEFAULT_FORMAT = "^[a-z0-9]+$";
	
	@Autowired
	private ApplicationContext context;
	
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
	
	private ComponentsConfiguration componentsConfiguration;
	
	/**
	 * Creates a map that links component ID to the connector for such component.
	 * 
	 * @return Map: component ID -> connector for the component
	 */
	@Bean
	public Map<String, Component> getComponentsMap() {
		Map<String, Component> componentMap = new HashMap<String, Component>();
		String missingComponentIdErr = "The application-components.yml configuration file is invalid: one of the components is missing the following required property: ";
		String missingFieldErr = "The application-components.yml configuration file is invalid: component %s is missing the following required property: %s";
		String componentId, implementation, roles, format;
		
		// Reads components configuration from YAML file
		Yaml yaml = new Yaml(new Constructor(ComponentsConfiguration.class));
		InputStream input = null;
		try {
			// Docker will have to mount the YAML configuration file to this path
			input = new FileInputStream(OrgManagerUtils.PATH_COMPONENTS_CONFIG);
		} catch (FileNotFoundException e) {
			input = ComponentsConfig.class.getResourceAsStream("/components.yml"); // default components configuration file
			if (input == null)
				throw new InvalidConfigurationException("Components configuration file " + OrgManagerUtils.PATH_COMPONENTS_CONFIG + " could not be found.");
		}
		componentsConfiguration = yaml.load(input); // Loads the file's content
		try {
			input.close();
		} catch (IOException e) {}
		for (Map<String, String> map : componentsConfiguration.getComponents()) {
			componentId = map.get(FIELD_COMPONENT_ID);
			if (componentId == null || componentId.equals("")) // componentId property is required
				throw new InvalidConfigurationException(missingComponentIdErr + FIELD_COMPONENT_ID);
			implementation = map.get(FIELD_IMPLEMENTATION);
			if (implementation == null || implementation.equals("")) // implementation property is required
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, FIELD_IMPLEMENTATION));
			roles = map.get(FIELD_ROLES);
			if (roles == null || roles.equals("")) // roles property is required
				throw new InvalidConfigurationException(String.format(missingFieldErr, componentId, FIELD_ROLES));
			format = map.get(FIELD_FORMAT);
			if (format == null || format.equals("")) // when missing, format is given a default value
				map.put(FIELD_FORMAT, DEFAULT_FORMAT);
			Component component = (Component) context.getBean(implementation); // instantiates the component with a subclass
			component.init(map); // initializes the component, passing its properties to it
			componentMap.put(componentId, component); // adds the component to the map
		}
		
		return componentMap;
	}
	
	public List<Map<String, String>> getComponentProperties() {
		return componentsConfiguration.getComponents();
	}
}