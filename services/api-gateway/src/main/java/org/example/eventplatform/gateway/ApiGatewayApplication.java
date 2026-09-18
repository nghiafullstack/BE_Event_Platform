package org.example.eventplatform.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;

// shared-common pulls in spring-boot-starter-data-jpa (for BaseEntity / GlobalExceptionHandler's
// EntityNotFoundException), but this service has no database of its own.
@SpringBootApplication(scanBasePackages = "org.example.eventplatform",
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
