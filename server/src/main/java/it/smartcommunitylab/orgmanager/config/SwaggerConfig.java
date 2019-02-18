/*******************************************************************************
 * Copyright 2015 Fondazione Bruno Kessler
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package it.smartcommunitylab.orgmanager.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.AuthorizationCodeGrantBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.TokenEndpoint;
import springfox.documentation.service.TokenRequestEndpoint;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {                       
	
	@Autowired
	private SwaggerConf conf;
	
	@Value("${aac.uri}")
	private String AUTH_SERVER;
	private static final String CLIENT_SECRET = "<your-client-secret>";
	private static final String CLIENT_ID = "<your-client-id>";

	@Bean
	@ConfigurationProperties("swagger")
	public SwaggerConf getConf(){
		return new SwaggerConf();
	}
    
	/***************** Core AAC API - PROFILES, TOKEN INTROSPECTION **********/ 
    @Bean
    public Docket api() { 
        return new Docket(DocumentationType.SWAGGER_2)
          .groupName("Org Manager")
          .apiInfo(apiInfo(conf.title.get("orgmanager"), conf.description.get("orgmanager")))
          .select()                                  
          .apis(RequestHandlerSelectors.basePackage("it.smartcommunitylab.orgmanager"))
          .build()
          .securitySchemes(Arrays.asList(
        		  securitySchemeUser(scopesUser())
        		  ))
          .securityContexts(Arrays.asList(
        		  securityContext(scopesUser(), ".*", "spring_oauth")
        		  ));                                           
    }
	private AuthorizationScope[] scopesUser() {
		AuthorizationScope[] scopes = { 
				new AuthorizationScope("profile.basicprofile.me", "Basic profile of the current platform user. Read access only."),
				new AuthorizationScope("user.roles.me", "Read roles of the current user.")
		};
		return scopes;
	}

    
    public ApiInfo apiInfo(String title, String description) {
        return new ApiInfo(
          title, 
          description, 
          conf.version, 
          null, 
          new Contact(conf.contact.get("name"), conf.contact.get("url"), conf.contact.get("email")), 
          conf.license, 
          conf.licenseUrl, 
          Collections.emptyList());
    }
    
	private SecurityScheme securitySchemeUser(AuthorizationScope[] scopes) {
		GrantType acGrantType = new AuthorizationCodeGrantBuilder()
				.tokenEndpoint(new TokenEndpoint(AUTH_SERVER + "oauth/token", "oauthtoken"))
				.tokenRequestEndpoint(new TokenRequestEndpoint(AUTH_SERVER + "eauth/authorize", CLIENT_ID, CLIENT_SECRET)).build();
		SecurityScheme oauth = new OAuthBuilder().name("spring_oauth")
				.grantTypes(Arrays.asList(acGrantType)).scopes(Arrays.asList(scopes)).build();
		return oauth;
	}
	
	private SecurityContext securityContext(AuthorizationScope[] scopes, String path, String type) {
        return SecurityContext.builder()
        		.securityReferences(Arrays.asList(new SecurityReference(type, scopes)))
        		.forPaths(PathSelectors.regex(path))
        		.build();
    }
    
    public static class SwaggerConf {
		private HashMap<String, String> title;
		private HashMap<String, String> description;
		private HashMap<String, String> contact;
		private String version;
		private String license;
		private String licenseUrl;
		
		public HashMap<String, String> getTitle() {
			return title;
		}
		public void setTitle(HashMap<String, String> title) {
			this.title = title;
		}
		public HashMap<String, String> getDescription() {
			return description;
		}
		public void setDescription(HashMap<String, String> description) {
			this.description = description;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getLicense() {
			return license;
		}
		public void setLicense(String license) {
			this.license = license;
		}
		public String getLicenseUrl() {
			return licenseUrl;
		}
		public void setLicenseUrl(String licenseUrl) {
			this.licenseUrl = licenseUrl;
		}
		public HashMap<String, String> getContact() {
			return contact;
		}
		public void setContact(HashMap<String, String> contact) {
			this.contact = contact;
		}
   }	
}