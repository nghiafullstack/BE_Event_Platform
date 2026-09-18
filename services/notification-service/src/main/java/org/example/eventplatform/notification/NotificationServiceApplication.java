package org.example.eventplatform.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;

// shared-common pulls in spring-boot-starter-data-jpa (for BaseEntity), but this
// service has no database of its own — FCM tokens live in Redis — so JPA/DataSource
// auto-configuration must be excluded or startup fails looking for a datasource.
@SpringBootApplication(scanBasePackages = "org.example.eventplatform",
        exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
