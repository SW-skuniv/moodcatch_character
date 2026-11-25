package com.moodcatch.global.config;

import com.moodcatch.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * prod 프로필에서 Swagger와 API 문서가 꺼져 있는지 확인한다.
 * 실제 DB/Redis 접속 정보는 Testcontainers(@ServiceConnection)가 덮어쓰므로,
 * prod 설정의 환경변수 자리표시자에는 쓰이지 않는 더미 값만 넣는다.
 */
@SpringBootTest(properties = {
        "DB_URL=postgresql://unused:5432/unused",
        "DB_USERNAME=unused",
        "DB_PASSWORD=unused",
        "REDIS_URL=redis://unused:6379"
})
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Import(TestcontainersConfiguration.class)
class ProdSwaggerDisabledTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("prod에서는 Swagger UI가 열리지 않는다")
    void swaggerUi_isDisabledInProd() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("prod에서는 OpenAPI 문서가 열리지 않는다")
    void apiDocs_isDisabledInProd() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("prod에서도 헬스체크는 동작한다")
    void healthCheck_worksInProd() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }
}
