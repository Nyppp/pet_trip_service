package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.service.ScheduleService;
import com.oreumi.pet_trip_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/schedules")
public class ScheduleController {
    private final ScheduleService scheduleService;
    private final UserService userService;

    @GetMapping
    public String showScheduleList(@PathVariable Long userId,
                                   @RequestParam Long placeId,
                                   Authentication auth,
                                   Model model){

        String email = auth.getName();
        User user = userService.findUserByEmail(email).orElseThrow();

        if(!user.getId().equals(userId)) throw new AccessDeniedException("스케쥴 접근 권한이 없습니다.");
        model.addAttribute("userId", userId);

        model.addAttribute("placeId", placeId);

        return "/schedule/schedule_list";
    }

    @GetMapping("/new")
    public String showScheduleForm(@PathVariable Long userId,
                                   Model model){
        String formAction = String.format("/schedule/new");

        //스케쥴 새로 만들기
        model.addAttribute("scheduleDTO", new ScheduleDTO());
        model.addAttribute("isNew", true);

        return "/schedule/schedule_create";
    }

    @GetMapping("/{scheduleId}/edit")
    public String showScheduleEditForm(@PathVariable("userId") Long userId,
                                       @PathVariable("scheduleId") Long scheduleId,
                                       Model model){

        //새로 만들기와 차이 : 수정하기 버튼으로 진입 + PathVariable로 id 넘겨줌
        //서비스 구현 > id 접근 후, 내용 수정하여 저장
        String formAction = String.format("/schedule/" + scheduleId + "/edit");
        model.addAttribute("isNew", false);

        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("스케쥴을 찾을 수 없습니다."));

        ScheduleDTO dto = new ScheduleDTO(schedule);

        model.addAttribute("scheduleDTO", dto);

        return "/schedule/schedule_create";
    }

    @GetMapping("/users/{id}/schedules/{scheduleId}")
    public String showScheduleItemList(@PathVariable("id") Long scheduleId,
                                       Model model){
        //스케쥴 객체 로딩
        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId)
                .orElseThrow();

        model.addAttribute("schedule", schedule);

        return "/schedule/schedule_detail";
    }
}
