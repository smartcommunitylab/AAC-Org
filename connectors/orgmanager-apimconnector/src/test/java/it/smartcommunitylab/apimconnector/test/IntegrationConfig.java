package it.smartcommunitylab.apimconnector.test;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import it.smartcommunitylab.apimconnector.APIMConnector;
import it.smartcommunitylab.apimconnector.utils.ApimConstants;

@Configuration
@PropertySource("classpath:application.properties")
public class IntegrationConfig {

	@Value("${usermgmtEndpoint}")
	private String usermgmtEndpoint;
	@Value("${usermgmtPassword}")
	private String usermgmtPassword;
	@Value("${multitenancyEndpoint}")
	private String tenantmgmtEndpoint;
	@Value("${multitenancyPassword}")
	private String tenantmgmtPassword;
	@Value("${host}")
	private String host;
	
    public @Bean APIMConnector getAPIMConnector() {
    	APIMConnector apimConn = new APIMConnector();
    	Map<String,String> properties= new HashMap<>();
    	properties.put(ApimConstants.HOST, host);
    	properties.put(ApimConstants.USER_MGT_ENDPOINT, usermgmtEndpoint);
    	properties.put(ApimConstants.USER_MGT_PASSWORD, usermgmtPassword);
    	properties.put(ApimConstants.TENANT_MGT_ENDPOINT, tenantmgmtEndpoint);
    	properties.put(ApimConstants.TENANT_MGT_PASSWORD, tenantmgmtPassword);
    	apimConn.init(properties);
    	return apimConn;
    }
}