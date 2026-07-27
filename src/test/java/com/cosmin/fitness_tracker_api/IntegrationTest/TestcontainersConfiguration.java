package com.cosmin.fitness_tracker_api.IntegrationTest;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    MySQLContainer mySQLContainer() {
        return new  MySQLContainer("mysql:8.0")
                .withDatabaseName("fitness_tracker_test")
                .withUsername("test")
                .withPassword("test");
    }
}
