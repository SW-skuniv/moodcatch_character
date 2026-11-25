package com.moodcatch.global.config;

import com.moodcatch.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("헬스체크는 인증 없이 호출된다")
    void healthCheck_withoutAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CSRF 토큰 없는 POST도 보안 필터에서 막히지 않는다 (내부 호출 전용)")
    void post_withoutCsrfToken_isNotBlockedBySecurity() throws Exception {
        // /api/health는 GET만 있으므로 보안 필터를 통과하면 405가 나와야 한다 (막히면 403)
        mockMvc.perform(post("/api/health"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Spring Security 기본 계정(user)이 만들어지지 않는다")
    void defaultUser_isNotCreated() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("user"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    @DisplayName("인증 없이 호출해도 로그인 페이지로 보내지 않고 MVC 응답을 받는다")
    void unknownPath_withoutAuth_returns404() throws Exception {
        mockMvc.perform(get("/api/not-exist"))
                .andExpect(status().isNotFound());
    }
}
