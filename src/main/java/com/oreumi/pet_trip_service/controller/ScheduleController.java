package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.ScheduleService;
import com.oreumi.pet_trip_service.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
                                   @AuthenticationPrincipal CustomUserPrincipal principal,
                                   @RequestParam(required = false) Long placeId,
                                   Authentication auth,
                                   Model model){

        if(!userId.equals(principal.getUser().getId())) throw new AccessDeniedException("스케쥴 접근 권한이 없습니다.");
        model.addAttribute("userId", userId);
        model.addAttribute("placeId", placeId);

        return "/schedule/schedule_list";
    }

    @GetMapping("/new")
    public String showScheduleForm(@PathVariable Long userId,
                                   @RequestParam(required = false) Long placeId,
                                   @AuthenticationPrincipal CustomUserPrincipal principal,
                                   Model model){

        if(!userId.equals(userId)) throw new AccessDeniedException("스케쥴 접근 권한이 없습니다.");

        //스케쥴 새로 만들기
        model.addAttribute("scheduleDTO", new ScheduleDTO());
        model.addAttribute("isNew", true);
        model.addAttribute("placeId", placeId);

        return "/schedule/schedule_create";
    }

    @GetMapping("/{scheduleId}/edit")
    public String showScheduleEditForm(@PathVariable("userId") Long userId,
                                       @PathVariable("scheduleId") Long scheduleId,
                                       @AuthenticationPrincipal CustomUserPrincipal principal,
                                       Model model){

        if(!userId.equals(principal.getUser().getId())) throw new AccessDeniedException("스케쥴 접근 권한이 없습니다.");

        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("스케쥴을 찾을 수 없습니다."));

        ScheduleDTO dto = new ScheduleDTO(schedule);

        model.addAttribute("scheduleDTO", dto);
        model.addAttribute("isNew", false);

        return "/schedule/schedule_create";
    }
}
