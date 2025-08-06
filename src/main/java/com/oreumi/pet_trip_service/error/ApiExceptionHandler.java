package com.oreumi.pet_trip_service.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//Swagger UI와 충돌이 발생해서, 우선 사용X

//@RestControllerAdvice(basePackages = {"com.oreumi.pet_trip_service"})
public class ApiExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleApiException(Exception ex, HttpServletRequest request) {
        String uri = request.getRequestURI();

        return ResponseEntity.status(500)
                .body(new ErrorResponse(500, "API 서버 오류", ex.getMessage()));
    }
}