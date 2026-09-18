package org.example.eventplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;

// Only the packages this WebFlux app can actually load: shared.security (JwtTokenProvider —
// no servlet dependency) and this service's own code. shared.web/shared.exception exist for
// WebMVC services (ResponseWrappingAdvice implements the Servlet-flavored ResponseBodyAdvice
// interface directly), which crashes class scanning here since spring-webmvc isn't on this
// module's classpath at all — this app has no annotated @RestController to wrap anyway.
// shared-common still brings spring-boot-starter-data-jpa as a Maven dependency regardless of
// what's scanned, so its auto-configuration must still be excluded (no database here).
@SpringBootApplication(scanBasePackages = {"org.example.eventplatform.gateway", "org.example.eventplatform.shared.security"},
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
