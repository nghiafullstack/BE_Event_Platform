package org.example.eventplatform.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

// shared-common brings spring-boot-starter-security transitively (for JwtTokenProvider's own
// dependency), which would otherwise auto-configure reactive HTTP Basic auth with a random
// generated password. GatewayProxyFilter (a plain WebFilter, unrelated to Spring Security) does
// the actual JWT check, so Spring Security itself just needs to get out of the way here.
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange.anyExchange().permitAll())
                .build();
    }
}
