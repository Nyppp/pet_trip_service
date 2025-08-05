package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public String showScheduleList(Model model){
        List<Schedule> scheduleList = scheduleService.findScheduleAllSchedules();
        model.addAttribute("schedules", scheduleList);
        return "/schedule/schedule_list";
    }

    @GetMapping("/new")
    public String showScheduleForm(Model model){
        
        //스케쥴 새로 만들기
        model.addAttribute("scheduleDTO", new ScheduleDTO());
        return "/schedule/schedule_create";
    }

    @GetMapping("/{id}/edit")
    public String showScheduleEditForm(@PathVariable("id") Long scheduleId,
                                       Model model){

        //새로 만들기와 차이 : 수정하기 버튼으로 진입 + PathVariable로 id 넘겨줌
        //서비스 구현 > id 접근 후, 내용 수정하여 저장

        return "/schedule/schedule_create";
    }

    @PostMapping("/new")
    public String createNewSchedule(@Valid @ModelAttribute ScheduleDTO scheduleDTO,
                                    Model model){

        //제목, 시작일, 종료일 정도만 우선 받고 > 스케쥴 서비스 > 저장
        //세션 or AuthenticationPrincipal 접근 > 유저 정보 불러와서 > 유저 리스트 > 해당 스케쥴 add 후 유저 정보 저장

        //임시 코드
        scheduleService.saveSchedule(scheduleDTO);


        return "redirect:/schedule";
    }

    @PatchMapping("/{id}/edit")
    public String editSchedule(@PathVariable("id") Long scheduleId,
                               @RequestParam String title,
                               @RequestParam LocalDate startDate,
                               @RequestParam LocalDate endDate,
                               Model model){
        //새로 생성과 동일하게 적용
        //하위에 있던 스케쥴 리스트 > 날짜가 바뀌면서 어떻게 처리할지?

        //1. 하위 스케쥴 모두 제거
        //2. 날짜 범위를 벗어난 스케쥴 모두 제거

        return "/schedule/schedule_list";
    }
}
