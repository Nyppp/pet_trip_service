package com.oreumi.pet_trip_service.error;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Hidden
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    /** 공통 응답 빌더 */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }

    /** 400: 잘못된 요청 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        // 내부 로그는 상세히, 사용자 메시지는 안전하게
        log.warn("Bad request: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_REQUEST, "잘못된 입력 요청입니다, 입력값을 다시 확인해주세요");
        // 필요하면 개발환경에서만 ex.getMessage()를 노출하도록 스위치 가능
    }

    /** 400: @Valid 검증 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var firstMsg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("요청 값이 유효하지 않습니다.");
        log.warn("Validation failed: {}", firstMsg, ex);
        return build(HttpStatus.BAD_REQUEST, firstMsg);
    }

    /** 403: 권한 없음 */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
        log.warn("Forbidden: {}", ex.getMessage(), ex);
        return build(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    /** 405: 지원하지 않는 메서드 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not allowed: {}", ex.getMessage(), ex);
        return build(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다.");
    }

    // 404/400: JPA 엔티티 없음(getReferenceById 접근 등)
    @ExceptionHandler(jakarta.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFound(jakarta.persistence.EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage(), ex);
        return build(HttpStatus.NOT_FOUND, "요청하신 정보를 찾을 수 없습니다.");
    }

    // 400: JSON 바디 파싱/형변환 실패 (빈 바디, 날짜 포맷, 숫자↔문자 등)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> notReadable(Exception ex) {
        log.warn("Not readable: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_REQUEST, "요청 본문을 해석할 수 없습니다.");
    }

    // 400: 경로변수/쿼리파라미터 타입 불일치 (?id=abc)
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> typeMismatch(Exception ex) {
        log.warn("Type mismatch: {}", ex.getMessage(), ex);
        return build(HttpStatus.BAD_REQUEST, "요청 값 타입이 올바르지 않습니다.");
    }

    // 409: 무결성 위반(중복키, FK 위반 등)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrity(org.springframework.dao.DataIntegrityViolationException ex) {
        log.warn("Integrity violation: {}", ex.getMessage(), ex);
        return build(HttpStatus.CONFLICT, "데이터 무결성 제약을 위반했습니다.");
    }

    /** 500: 그 외 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternal(Exception ex) {
        // 스택트레이스 포함하여 ERROR 레벨로 기록
        log.error("Internal server error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }
}