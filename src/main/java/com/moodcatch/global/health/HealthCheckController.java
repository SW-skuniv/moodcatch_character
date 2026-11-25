package com.moodcatch.global.health;

import com.moodcatch.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    private static final String STATUS_UP = "UP";

    @Operation(summary = "서버 상태 확인", description = "서버가 요청을 받을 수 있는 상태인지 확인합니다.")
    @GetMapping
    public BaseResponse<String> healthCheck() {
        return BaseResponse.success("서버가 정상 동작 중입니다.", STATUS_UP);
    }
}
