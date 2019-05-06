package it.smartcommunitylab.apimconnector.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import it.smartcommunitylab.apimconnector.APIMConnector;

@Configuration
@PropertySource("classpath:application.properties")
public class IntegrationConfig {

	@Value("${usermgmt.endpoint}")
	private String usermgmtEndpoint;
	@Value("${usermgmt.password}")
	private String usermgmtPassword;
	@Value("${multitenancy.endpoint}")
	private String tenantmgmtEndpoint;
	@Value("${multitenancy.password}")
	private String tenantmgmtPassword;
	
    public @Bean APIMConnector getAPIMConnector() {
    	APIMConnector apimConn = new APIMConnector();
    	Map<String,String> properties= new HashMap<>();
    	properties.put("usermgmtEndpoint", usermgmtEndpoint);
    	properties.put("usermgmtPassword", usermgmtPassword);
    	properties.put("multitenancyEndpoint", tenantmgmtEndpoint);
    	properties.put("multitenancyPassword", tenantmgmtPassword);
    	apimConn.init(properties);
    	return apimConn;
    }
}