package com.oreumi.pet_trip_service.error;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Hidden
@Slf4j
@ControllerAdvice
public class ErrorHandler {
    private static final String DEFAULT_ERROR_VIEW = "error/error_page";

    @ExceptionHandler(EntityNotFoundException.class)
    public ModelAndView handleEntityNotFound(EntityNotFoundException e,
                                             HttpServletRequest request) {
        log.warn("Entity not found: {}", e.getMessage());

        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("errorCode", "NOT_FOUND");
        mav.addObject("message", "요청하신 페이지를 찾을 수 없습니다.");
        mav.setStatus(HttpStatus.NOT_FOUND);

        return mav;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied(AccessDeniedException e,
                                           HttpServletRequest request) {
        log.warn("Access denied: {}", e.getMessage());

        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("errorCode", "ACCESS_DENIED");
        mav.addObject("message", "접근 권한이 없습니다.");
        mav.setStatus(HttpStatus.FORBIDDEN);

        return mav;
    }

    @ExceptionHandler(ValidationException.class)
    public ModelAndView handleValidation(ValidationException e,
                                         HttpServletRequest request) {
        log.info("Validation error: {}", e.getMessage());

        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("errorCode", "VALIDATION_ERROR");
        mav.addObject("message", e.getMessage());
        mav.setStatus(HttpStatus.BAD_REQUEST);

        return mav;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSize(MaxUploadSizeExceededException e,
                                            HttpServletRequest request) {
        log.warn("File upload size exceeded");

        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("errorCode", "FILE_TOO_LARGE");
        mav.addObject("message", "파일 크기가 제한을 초과했습니다. (최대 10MB)");
        mav.setStatus(HttpStatus.BAD_REQUEST);

        return mav;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResource(NoResourceFoundException e,
                                            HttpServletRequest request) {
        log.warn("Not found resource");

        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("errorCode", "NOT_FOUND_RESOURCE");
        mav.addObject("message", "리소스 혹은 페이지를 찾을 수 없습니다.");
        mav.setStatus(HttpStatus.BAD_REQUEST);

        return mav;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGeneral(Exception e, HttpServletRequest request) {
        // 예상치 못한 에러만 상세 로깅
        log.error("Unexpected error occurred", e);

        ModelAndView mav = new ModelAndView(DEFAULT_ERROR_VIEW);
        mav.addObject("errorCode", "INTERNAL_ERROR");
        mav.addObject("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        return mav;
    }

    private boolean isDevelopmentEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "dev");
        return "dev".equals(profile) || "local".equals(profile);
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}