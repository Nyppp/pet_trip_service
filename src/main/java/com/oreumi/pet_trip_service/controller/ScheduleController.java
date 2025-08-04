package com.oreumi.pet_trip_service.controller;


import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleController {
    @GetMapping("/schedule")
    public String showScheduleList(){
        return "/schedule/schedule_list";
    }
}
