package it.smartcommunitylab.orgmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.web.bind.annotation.RestController;

import it.smartcommunitylab.aac.AACService;

@Configuration
@EnableResourceServer
@RestController
public class SecurityConfig extends ResourceServerConfigurerAdapter {
		
	@Value("${security.oauth2.client.organizationManagementScope}")
	private String orgMgmtScope;
	
	@Value("${security.oauth2.client.tokenInfoUri}")
	private String tokenInfoUri;
	
	@Value("${security.oauth2.client.tokenName}")
	private String tokenName;
	
	@Value("${security.oauth2.client.userIdField}")
	private String userIdField;
	
	@Value("${security.oauth2.client.clientId}")
	private String clientId;
	
	@Value("${security.oauth2.client.clientSecret}")
	private String clientSecret;
	
	@Value("${aac.uri}")
	private String aacUri;
	
	@Value("${aac.apis.manageRolesApi}")
	private String manageRolesApi;
	
	@Value("${aac.apis.userProfilesApi}")
	private String userProfilesApi;
	
	@Value("${aac.apis.currentUserRolesApi}")
	private String currentUserRolesApi;
	
	@Value("${aac.apis.currentUserProfileApi}")
	private String currentUserProfileApi;
	
	public String getOrganizationManagementScope() {
		return orgMgmtScope; // scope that denotes certain privileges in creating, updating, configuring and deleting organizations
	}
	
	public String getTokenInfoUri() {
		return tokenInfoUri; // identity provider API endpoint to check the token's information
	}
	
	public AACService getAACService() {
		return new AACService(aacUri, clientId, clientSecret); // service for various operations on AAC
	}
	
	public String getManageRolesUri() {
		return aacUri + manageRolesApi; // identity provider API endpoint to add or remove roles
	}
	
	public String getUserProfilesUri() {
		return aacUri + userProfilesApi; // identity provider API endpoint to retrieve user profiles
	}
	
	public String getCurrentUserRolesUri() {
		return aacUri + currentUserRolesApi; // identity provider IP API endpoint to list the current user's roles
	}
	
	public String getCurrentUserProfileUri() {
		return aacUri + currentUserProfileApi; // identity provider API endpoint to get the current user's basic profile
	}
	
	public String getTokenName() {
		return tokenName; // Name of the token parameter to use with tokenInfoUri
	}
	
	public String getUserIdField() {
		return userIdField; // Name of the field that contains the username in the identity provider's token information API's response
	}
	
	/**
	 * Configures access to the API endpoints.
	 */
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().antMatchers("/", "/login**", "/webjars/**", "/error**", "/swagger*", "/v2/api-docs**").permitAll()
				.anyRequest().authenticated().and().logout().logoutSuccessUrl("/").permitAll()
				.and().cors().and().csrf().disable();
	}
	
	/**
	 * Configures the resource ID to the application's client ID. Without this, authentication would fail.
	 */
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId(clientId);
	}
	
	/**
	 * Bean to validate the token.
	 * 
	 * @return - RemoteTokenServices bean
	 */
	@Primary
	@Bean
	public RemoteTokenServices tokenService() {
		RemoteTokenServices tokenServices = new RemoteTokenServices();
		tokenServices.setCheckTokenEndpointUrl(tokenInfoUri); // endpoint to check token information
		tokenServices.setClientId(clientId);
		tokenServices.setClientSecret(clientSecret);
		return tokenServices;
	}
	
}