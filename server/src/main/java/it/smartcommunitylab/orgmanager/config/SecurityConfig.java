package it.smartcommunitylab.orgmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import it.smartcommunitylab.aac.security.jwt.JwtValidatingDecoder;
import it.smartcommunitylab.aac.security.jwt.JwtAudienceValidator;
import it.smartcommunitylab.aac.security.jwt.JwtAuthenticationConverter;
import it.smartcommunitylab.aac.security.jwt.authority.JwtScopeAuthoritiesConverter;
import it.smartcommunitylab.aac.security.jwt.authority.JwtSpaceAwareAuthoritiesRoleConverter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${security.oauth2.resourceserver.client-id}")
    private String clientId;

    /**
     * Configures access to the API endpoints.
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
                .antMatchers("/api/auth/login", "/api/auth/callback", "/api/auth/user", "api/auth/profile").permitAll()
                .antMatchers("/", "/login**", "/webjars/**", "/error**", "/swagger*", "/v2/api-docs**").permitAll()
                .antMatchers("/api/**").authenticated()
//                .anyRequest().authenticated().and().logout().logoutSuccessUrl("/").permitAll()
                .and()
                .oauth2ResourceServer()
                .jwt()
                .jwtAuthenticationConverter(jwtTokenConverter());
    }

    @Bean
    JwtDecoder jwtDecoder() {
        JwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);
        // we need to validate audience in addition to issuer
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtAudienceValidator(clientId);
        return new JwtValidatingDecoder(jwtDecoder, audienceValidator);
    }

    Converter<Jwt, AbstractAuthenticationToken> jwtTokenConverter() {
        return new JwtAuthenticationConverter(
                new JwtSpaceAwareAuthoritiesRoleConverter(),
                new JwtScopeAuthoritiesConverter());
        // example: assign any user a default role
//        return new JwtAuthenticationConverter(
//                new ComponentAwareAuthoritiesRoleConverter(component),
//                new ScopeAuthoritiesConverter(),
//                new SimpleUserAuthoritiesConverter());
    }

//
//    /**
//     * Configures the resource ID to the application's client ID. Without this,
//     * authentication would fail.
//     */
//    @Override
//    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
//        resources.resourceId(clientId);
//    }
//
//    @Bean
//    public TokenStore tokenStore() {
//        return new JwkTokenStore(jwkSetUri, jwtAccessTokenConverter());
//    }
//
//    @Bean
//    public AccessTokenConverter accessTokenConverter() {
//        return new ClaimAwareAccessTokenConverter();
//    }
//
//    @Bean
//    public JwtAccessTokenConverter jwtAccessTokenConverter() {
//        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
//        converter.setAccessTokenConverter(accessTokenConverter());
//        return converter;
//    }
//
//    public static class ClaimAwareAccessTokenConverter extends DefaultAccessTokenConverter {
//
//        @Override
//        public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
//            HashMap<String, Object> copy = new HashMap<>(claims);
//            if (!claims.containsKey(AUTHORITIES))
//                copy.put(AUTHORITIES, claims.get("roles"));
//
//            System.out.println(copy.toString());
//
//            OAuth2Authentication authentication = super.extractAuthentication(copy);
//            ((UsernamePasswordAuthenticationToken) authentication.getUserAuthentication()).setDetails(claims);
//            return authentication;
//        }
//    }

}