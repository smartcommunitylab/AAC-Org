package it.smartcommunitylab.orgmanager.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.smartcommunitylab.orgmanager.common.InvalidConfigurationException;
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
	
	private List<Map<String, String>> components;
	
	/**
	 * Creates a map that links component ID to the connector for such component.
	 * 
	 * @return Map: component ID -> connector for the component
	 */
	@Bean
	public Map<String, Component> getComponents() {
		Map<String, Component> componentMap = new HashMap<String, Component>();
		
		String missingFieldErr = "Organization Manager is not correctly configured: a component defined in application-components.yml is missing the following field: "; 
		String componentId, implementation, roles, format;
		for (Map<String, String> map : components) {
			componentId = map.get(FIELD_COMPONENT_ID);
			if (componentId == null || componentId.equals("")) // componentId property is required
				throw new InvalidConfigurationException(missingFieldErr + FIELD_COMPONENT_ID);
			implementation = map.get(FIELD_IMPLEMENTATION);
			if (implementation == null || implementation.equals("")) // implementation property is required
				throw new InvalidConfigurationException(missingFieldErr + FIELD_IMPLEMENTATION);
			roles = map.get(FIELD_ROLES);
			if (roles == null || roles.equals("")) // roles property is required
				throw new InvalidConfigurationException(missingFieldErr + FIELD_ROLES);
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
		return components;
	}
	
	public void setComponents(List<Map<String, String>> components) {
		this.components = components;
	}
}
