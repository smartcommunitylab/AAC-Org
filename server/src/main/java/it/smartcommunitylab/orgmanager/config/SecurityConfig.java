package it.smartcommunitylab.orgmanager.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;

import it.smartcommunitylab.aac.AACProfileService;
import it.smartcommunitylab.aac.AACRoleService;
import it.smartcommunitylab.aac.AACService;
import it.smartcommunitylab.aac.model.TokenData;
import it.smartcommunitylab.orgmanager.common.Constants;
import it.smartcommunitylab.orgmanager.common.IdentityProviderAPIException;

@Configuration
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

	
	@Value("${security.oauth2.client.organizationManagementScope}")
	private String orgMgmtScope;
	
	@Value("${security.oauth2.client.clientId}")
	private String clientId;
	
	@Value("${security.oauth2.client.clientSecret}")
	private String clientSecret;
	
	@Value("${aac.uri}")
	private String aacUri;
		
	@Value("${security.oauth2.resource.jwk.keySetUri}")
	private String jwkSetUri;
	
	private AACService aacService;
	private ConcurrentHashMap<String, TokenData> tokens = new ConcurrentHashMap<>();
	
	/**
	 * Initializes the service to obtain client tokens.
	 */
	@PostConstruct
	private void init() {
		// Generates the service to obtain the proper client tokens needed for certain calls to the identity provider's APIs
		aacService = new AACService(aacUri, clientId, clientSecret);
	}

	
	public String getOrganizationManagementScope() {
		return orgMgmtScope; // scope that denotes certain privileges in creating, updating, configuring and deleting organizations
	}
	
	public AACRoleService getAACRoleService() {
		return new AACRoleService(aacUri); // service for various operations on AAC roles
	}
	public AACProfileService getAACProfileService() {
		return new AACProfileService(aacUri); // service for various operations on AAC Profiles
	}
	
	
	public String getToken(String scope) throws IdentityProviderAPIException {
		TokenData data = tokens.get(scope);
		if (data == null || (data.getExpires_on() - 1000*60*60) > System.currentTimeMillis()) {
			synchronized (tokens) {
				if (!tokens.containsKey(scope))
					try {
						TokenData dt = aacService.generateClientToken(scope);
						tokens.putIfAbsent(scope, dt);
					} catch (Exception e) {
						throw new IdentityProviderAPIException("Unable to generate an access token with the desired scope.");
					}
			}
			
		}
		return tokens.get(scope).getAccess_token();
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
	
	@Bean
    public TokenStore tokenStore() {
        return new JwkTokenStore(jwkSetUri, jwtAccessTokenConverter());
    }
 
	@Bean
	public AccessTokenConverter accessTokenConverter() {
		return new ClaimAwareAccessTokenConverter();
	}
	
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setAccessTokenConverter(accessTokenConverter());
        return converter;
    }

    public static class ClaimAwareAccessTokenConverter extends DefaultAccessTokenConverter {
    	 
        @Override
        public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
            OAuth2Authentication authentication = super.extractAuthentication(claims);
            ((UsernamePasswordAuthenticationToken)authentication.getUserAuthentication()).setDetails(claims);
            return authentication;
        }
    }
    
}