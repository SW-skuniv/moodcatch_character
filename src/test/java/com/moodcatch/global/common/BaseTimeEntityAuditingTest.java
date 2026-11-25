package com.moodcatch.global.common;

import com.moodcatch.TestcontainersConfiguration;
import fixture.auditing.AuditingTestEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

// 이 테스트에서만 테스트용 엔티티를 등록하고, 테이블은 Hibernate가 임시로 만든다
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Import({TestcontainersConfiguration.class, BaseTimeEntityAuditingTest.AuditingEntityScanConfig.class})
@Transactional
class BaseTimeEntityAuditingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("엔티티를 저장하면 생성일과 수정일이 자동으로 채워진다")
    void persist_fillsCreatedAtAndUpdatedAt() {
        AuditingTestEntity entity = new AuditingTestEntity();

        entityManager.persist(entity);
        entityManager.flush();

        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @TestConfiguration(proxyBeanMethods = false)
    @EntityScan(basePackages = {"com.moodcatch", "fixture.auditing"})
    static class AuditingEntityScanConfig {
    }
}
