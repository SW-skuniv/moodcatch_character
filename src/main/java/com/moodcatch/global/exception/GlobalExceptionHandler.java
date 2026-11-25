package com.moodcatch.global.exception;

import com.moodcatch.global.common.BaseResponse;
import com.moodcatch.global.exception.model.BaseErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

/**
 * 모든 예외를 BaseResponse 형식으로 바꾼다.
 * Spring MVC 표준 예외(404, 405, 파라미터 누락·타입 오류 등)는 ResponseEntityExceptionHandler가
 * 알맞은 상태 코드를 정하고, handleExceptionInternal에서 응답 본문만 BaseResponse로 바꾼다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 커스텀 예외
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<BaseResponse<Object>> handleCustomException(CustomException ex) {
        BaseErrorCode errorCode = ex.getErrorCode();
        log.warn("CustomException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage());
        return toResponse(errorCode);
    }

    // 메서드 보안(@PreAuthorize 등)에서 권한이 없을 때
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("권한 없음: {}", ex.getMessage());
        return toResponse(GlobalErrorCode.FORBIDDEN);
    }

    // 컨트롤러 안에서 인증 예외가 던져졌을 때
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<BaseResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("인증 실패: {}", ex.getMessage());
        return toResponse(GlobalErrorCode.UNAUTHORIZED);
    }

    // 클래스에 @Validated를 붙인 메서드 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("Validation 오류 발생: {}", ex.getMessage());
        return toResponse(GlobalErrorCode.INVALID_INPUT_VALUE);
    }

    // 예상치 못한 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
        log.error("Server 오류 발생: ", ex);
        return toResponse(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }

    // Validation 실패
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String errorMessages =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> String.format("[%s] %s", e.getField(), e.getDefaultMessage()))
                        .collect(Collectors.joining(" / "));
        log.warn("Validation 오류 발생: {}", errorMessages);
        return ResponseEntity.badRequest().body(BaseResponse.error(
                GlobalErrorCode.INVALID_INPUT_VALUE.getCode(), GlobalErrorCode.INVALID_INPUT_VALUE.getMessage()));
    }

    // 잘못된 JSON 형식으로 요청 시
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.error("JSON 파싱 에러 발생: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(BaseResponse.error("JSON400", "올바르지 않은 JSON 입력입니다."));
    }

    // 필수 헤더(토큰 등)가 누락되었을 때. 쿠키 누락 등 다른 바인딩 오류는 기본 처리(400, G001)로 보낸다
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(
            ServletRequestBindingException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex instanceof MissingRequestHeaderException) {
            return ResponseEntity.badRequest().body(BaseResponse.error("TOKEN400", "헤더 정보가 누락되었습니다."));
        }
        return super.handleServletRequestBindingException(ex, headers, status, request);
    }

    // 그 밖의 Spring MVC 표준 예외: 상태 코드는 Spring이 정한 값을 그대로 쓰고 본문만 바꾼다
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            log.error("Server 오류 발생: ", ex);
        } else {
            log.warn("요청 처리 실패 ({}): {}", statusCode.value(), ex.getMessage());
        }
        BaseResponse<Object> errorBody = GlobalErrorCode.fromStatus(statusCode)
                .map(errorCode -> BaseResponse.error(errorCode.getCode(), errorCode.getMessage()))
                .orElseGet(() -> BaseResponse.error(String.valueOf(statusCode.value()), reasonOf(statusCode)));
        return ResponseEntity.status(statusCode).headers(headers).body(errorBody);
    }

    private static ResponseEntity<BaseResponse<Object>> toResponse(BaseErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getStatus())
                .body(BaseResponse.error(errorCode.getCode(), errorCode.getMessage()));
    }

    private static String reasonOf(HttpStatusCode statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        return status != null ? status.getReasonPhrase() : "요청을 처리할 수 없습니다.";
    }
}
