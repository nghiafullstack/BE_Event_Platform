package org.example.eventplatform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

// Spring Boot 4 moved WebClient.Builder auto-configuration into its own
// starter (spring-boot-starter-webclient); simplest to just define the bean
// ourselves rather than pull in yet another module for one bean.
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
