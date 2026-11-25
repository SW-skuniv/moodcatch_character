package com.moodcatch.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseTimeEntity의 @CreatedDate, @LastModifiedDate를 채우기 위한 설정.
 * Application 클래스에 붙이면 @WebMvcTest 같은 슬라이스 테스트가 JPA 없이 뜨지 못해서 따로 둔다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
