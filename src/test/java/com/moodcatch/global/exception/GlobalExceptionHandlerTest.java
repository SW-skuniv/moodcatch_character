package com.moodcatch.global.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 500이 아니라 405를 반환한다")
    void methodNotSupported_returns405() throws Exception {
        mockMvc.perform(post("/test/ok"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.METHOD_NOT_ALLOWED.getCode()));
    }

    @Test
    @DisplayName("필수 요청 파라미터가 없으면 400을 반환한다")
    void missingRequestParam_returns400() throws Exception {
        mockMvc.perform(get("/test/param"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("파라미터 타입이 맞지 않으면 400을 반환한다")
    void typeMismatch_returns400() throws Exception {
        mockMvc.perform(get("/test/param").param("count", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("필수 헤더가 없으면 TOKEN400을 반환한다")
    void missingHeader_returnsToken400() throws Exception {
        mockMvc.perform(get("/test/header"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TOKEN400"));
    }

    @Test
    @DisplayName("헤더가 아닌 바인딩 오류(쿠키 누락)는 헤더 누락 메시지를 쓰지 않는다")
    void missingCookie_returnsInvalidInput() throws Exception {
        mockMvc.perform(get("/test/cookie"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.INVALID_INPUT_VALUE.getCode()));
    }

    @Test
    @DisplayName("권한 없음 예외는 403을 반환한다")
    void accessDenied_returns403() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    @DisplayName("인증 실패 예외는 401을 반환한다")
    void authenticationFailed_returns401() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    @DisplayName("CustomException은 에러 코드의 상태와 코드를 그대로 반환한다")
    void customException_returnsErrorCodeStatus() throws Exception {
        mockMvc.perform(get("/test/custom"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.RESOURCE_NOT_FOUND.getCode()));
    }

    @Test
    @DisplayName("예상하지 못한 예외만 500을 반환한다")
    void unexpectedException_returns500() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/ok")
        String ok() {
            return "ok";
        }

        @GetMapping("/test/param")
        String param(@RequestParam int count) {
            return String.valueOf(count);
        }

        @GetMapping("/test/header")
        String header(@RequestHeader("X-Token") String token) {
            return token;
        }

        @GetMapping("/test/cookie")
        String cookie(@CookieValue("session") String session) {
            return session;
        }

        @GetMapping("/test/forbidden")
        String forbidden() {
            throw new AccessDeniedException("denied");
        }

        @GetMapping("/test/unauthorized")
        String unauthorized() {
            throw new BadCredentialsException("bad credentials");
        }

        @GetMapping("/test/custom")
        String custom() {
            throw new CustomException(GlobalErrorCode.RESOURCE_NOT_FOUND);
        }

        @GetMapping("/test/boom")
        String boom() {
            throw new IllegalStateException("boom");
        }
    }
}
