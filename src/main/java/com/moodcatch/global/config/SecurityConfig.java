package com.moodcatch.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 이 서비스는 sw-back만 호출하는 내부 전용 서비스다.
 * 현재는 네트워크로만 접근을 막는다: docker-compose에서 sw-back 네트워크(sw-back_default)에만 붙고,
 * 호스트 포트는 127.0.0.1에만 열어 외부에서 직접 호출할 수 없다. 그래서 애플리케이션 단에서는 인증을 하지 않는다.
 *
 * 한계: sw-back_default에 붙은 컨테이너(sw-back, project_db, project_redis)는 모두 인증 없이 호출할 수 있다.
 * 즉 "sw-back 네트워크 전체를 신뢰"하는 구조다. sw-back이 사용자 입력 URL을 그대로 이쪽으로 넘기면(SSRF) 보호가 무력해진다.
 *
 * - CSRF: 브라우저 세션/쿠키를 쓰지 않는 서버 간 호출이라 끈다.
 * - formLogin/httpBasic: 사용자 로그인이 없으므로 끈다.
 *
 * TODO: 내부 API 키 인증으로 전환
 *  1. 공유 비밀 키를 환경변수(예: INTERNAL_API_KEY)로 주입하고 sw-back에도 같은 값을 둔다.
 *  2. OncePerRequestFilter로 X-Internal-Key 헤더를 검증한다.
 *     - 헤더가 없으면(null) 바로 실패 처리한다.
 *     - 길이가 노출되지 않도록 양쪽을 SHA-256으로 해시한 뒤 MessageDigest.isEqual로 비교한다.
 *     - 성공하면 SecurityContextHolder에 인증 객체(예: PreAuthenticatedAuthenticationToken)를 넣는다.
 *       넣지 않으면 authenticated()가 올바른 키로 온 요청까지 막는다.
 *     - shouldNotFilter에서 PUBLIC_PATHS는 건너뛴다.
 *  3. 필터는 @Component로 만들지 말고 new로 만들어 addFilterBefore(filter, AuthorizationFilter.class)로 등록한다.
 *     (@Component면 서블릿 필터로도 자동 등록되어 Security 체인 밖에서 한 번 더 돌고 PUBLIC_PATHS까지 막는다)
 *  4. 아래 anyRequest().permitAll()을 authenticated()로 바꾼다.
 *  5. 401/403 응답은 @RestControllerAdvice가 처리하지 못한다(필터 단계 예외라서).
 *     exceptionHandling의 authenticationEntryPoint/accessDeniedHandler에서 BaseResponse JSON을 직접 쓴다.
 *     (GlobalErrorCode.UNAUTHORIZED/FORBIDDEN 재사용. 설정하지 않으면 기본값이 403이라 401이 나가지 않는다)
 *  6. /actuator/** 도 인증 대상으로 할지 같이 정한다.
 */
@Configuration
public class SecurityConfig {

    // TODO: 내부 API 키 인증 전환 후에도 인증 없이 열어 둘 경로
    private static final String[] PUBLIC_PATHS = {
            "/swagger-ui",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        // TODO: 내부 API 키 인증 도입 시 authenticated()로 변경
                        .anyRequest().permitAll())
                .build();
    }

    /**
     * 사용자 로그인이 없는 서비스라 계정을 두지 않는다.
     * 이 빈이 없으면 Spring Boot가 기본 계정(user)과 랜덤 비밀번호를 만들고 기동 로그에 비밀번호를 찍는다.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("사용자 계정을 사용하지 않는 서비스입니다: " + username);
        };
    }
}
