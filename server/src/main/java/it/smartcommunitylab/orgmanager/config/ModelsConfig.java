package it.smartcommunitylab.orgmanager.config;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reads configuration for the components from the application-components.yml file.
 */
@Configuration
@EnableConfigurationProperties
public class ComponentsConfig {
	
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

}