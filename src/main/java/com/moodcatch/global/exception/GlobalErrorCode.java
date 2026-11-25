package com.moodcatch.global.exception;

import com.moodcatch.global.exception.model.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {

    INVALID_INPUT_VALUE("G001", "유효하지 않은 입력입니다.", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND("G002", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("G003", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    METHOD_NOT_ALLOWED("G004", "지원하지 않는 HTTP 메서드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    UNAUTHORIZED("G005", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("G006", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    /**
     * HTTP 상태에 대응하는 공통 에러 코드를 찾는다.
     * 같은 상태의 코드가 여러 개면 enum에 먼저 선언된 것을 쓴다.
     */
    public static Optional<GlobalErrorCode> fromStatus(HttpStatusCode statusCode) {
        return Arrays.stream(values())
                .filter(errorCode -> errorCode.status.value() == statusCode.value())
                .findFirst();
    }
}
