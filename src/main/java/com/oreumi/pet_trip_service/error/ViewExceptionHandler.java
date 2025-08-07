package com.oreumi.pet_trip_service.error;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

//Swagger UI와 충돌이 발생해서, 우선 사용X

//@ControllerAdvice(basePackages = {"com.oreumi.pet_trip_service"})
public class ViewExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleViewException(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "/";
    }
}