package com.oreumi.pet_trip_service.error;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

//Swagger UI와 충돌이 발생해서, 우선 사용X

@Hidden
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleApiException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                500,
                "API 서버 오류",
                ex.getMessage()
        );

        return ResponseEntity.ok()
                .body(error);
    }
}