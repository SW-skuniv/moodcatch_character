package com.moodcatch;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // 로컬/운영 DB(project_db)와 같은 메이저 버전으로 고정
        return new PostgreSQLContainer(DockerImageName.parse("postgres:18"));
    }

    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        // 공유 Redis(project_redis)와 같은 메이저 버전으로 고정
        return new GenericContainer<>(DockerImageName.parse("redis:8")).withExposedPorts(6379);
    }

}
