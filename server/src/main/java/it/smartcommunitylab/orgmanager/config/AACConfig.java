package it.smartcommunitylab.orgmanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import it.smartcommunitylab.aac.AACContext;

@Configuration
public class AACConfig {
    @Value("${aac.clientId}")
    private String clientId;

    @Value("${aac.clientSecret}")
    private String clientSecret;

    @Value("${aac.uri}")
    private String aacUri;

    @Bean
    public AACContext getContext() {
        return new AACContext(aacUri, clientId, clientSecret);
    }

}
