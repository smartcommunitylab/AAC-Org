package it.smartcommunitylab.apimconnector.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@PropertySource("classpath:application.properties")
public class TestConfig {

	@Value("${api.keystore.path}")
	private static String keystorePath;
	@Value("${api.multitenancy.endpoint}")
	private static String tenantEndpoint;
	
	@Bean
    public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
		System.setProperty("javax.net.ssl.trustStore", "/home/albana/Desktop/www/DigitalHub/APIM/wso2am-2.6.0/repository/resources/security/wso2carbon.jks");
		System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
		System.setProperty("javax.net.ssl.trustStoreType", "JKS");

		return new PropertySourcesPlaceholderConfigurer();
    }

}
