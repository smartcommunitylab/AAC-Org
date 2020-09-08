package it.smartcommunitylab.orgmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.smartcommunitylab.aac.AACContext;

@Configuration
public class AACConfig {
    @Value("${aac.client-id}")
    private String clientId;

    @Value("${aac.client-secret}")
    private String clientSecret;

    @Value("${aac.uri}")
    private String aacUri;

    @Bean
    public AACContext getContext() {
        return new AACContext(aacUri, clientId, clientSecret);
    }

}
