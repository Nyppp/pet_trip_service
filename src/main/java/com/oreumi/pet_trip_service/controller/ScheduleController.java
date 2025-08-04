package com.oreumi.pet_trip_service.controller;


import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    @GetMapping
    public String showScheduleList(){
        return "/schedule/schedule_list";
    }

    @GetMapping("/{id}")
    public String showScheduleDetail(@PathVariable Long scheduleId,
                                     Model model){
        //서비스 구현 > id 접근

        return "/schedule/schedule_detail";
    }

    @GetMapping("/new")
    public String showScheduleForm(){
        return "/schedule/schedule_create";
    }

    @GetMapping("/{id}/edit")
    public String showScheduleEditForm(@PathVariable Long scheduleId,
                                       Model model){

        //서비스 구현 > id 접근 후, 내용 수정하여 저장

        return "/schedule/schedule_create";
    }

}
