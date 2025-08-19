package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.PlaceImgService;
import com.oreumi.pet_trip_service.service.PlaceService;
import com.oreumi.pet_trip_service.service.ScheduleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequestMapping("/users/{userId}/schedules")
@RequiredArgsConstructor
public class ScheduleItemController {
    private final ScheduleService scheduleService;
    private final PlaceImgService placeImgService;
    private final PlaceService placeService;

    @GetMapping("/{scheduleId}")
    public String showScheduleItemList(@PathVariable Long userId,
                                       @PathVariable Long scheduleId,
                                       @AuthenticationPrincipal CustomUserPrincipal principal,
                                       Model model){

        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("스케쥴을 찾을 수 없습니다."));


        if(userId != principal.getUser().getId()){
            throw new AccessDeniedException("해당 스케쥴에 접근 권한이 없습니다.");
        }
        model.addAttribute("userId", userId);
        model.addAttribute("schedule" , schedule);

        return "/schedule/schedule_detail";
    }

    @GetMapping("/{scheduleId}/items/new")
    public String showScheduleItemForm(@PathVariable Long userId,
                                       @PathVariable Long scheduleId,
                                       @AuthenticationPrincipal CustomUserPrincipal principal,
                                       @RequestParam(required = false) Long placeId,
                                       Model model){

        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId).orElseThrow(
                ()->{throw  new EntityNotFoundException("존재하지 않는 스케쥴입니다.");});
        model.addAttribute("schedule", schedule);

        if(schedule.getUser().getId() != principal.getUser().getId()){
            throw new AccessDeniedException("해당 스케쥴에 접근 권한이 없습니다.");
        }

        ScheduleItemDTO scheduleItemDTO = new ScheduleItemDTO();
        if(placeId != null){
            PlaceDTO placeDTO = placeService.getPlaceDetail(placeId);

            scheduleItemDTO.setPlaceName(placeDTO.getName());
            scheduleItemDTO.setPlaceId(placeDTO.getId());
        }

        model.addAttribute("scheduleItemDTO", scheduleItemDTO);
        model.addAttribute("userId", userId);
        model.addAttribute("isNew", true);
        return "/schedule/schedule_item/schedule_item_create";
    }

    @GetMapping("/{scheduleId}/items/{itemId}")
    public String showScheduleItemDetail(@PathVariable Long userId,
                                         @AuthenticationPrincipal CustomUserPrincipal principal){
        if(userId != principal.getUser().getId()){
            throw new AccessDeniedException("해당 스케쥴에 접근 권한이 없습니다.");
        }

        return "/schedule/schedule_item/schedule_item_detail";
    }

    @GetMapping("/{scheduleId}/items/{itemId}/edit")
    public String showEditScheduleItemForm(@PathVariable Long userId,
                                           @PathVariable Long scheduleId,
                                           @AuthenticationPrincipal CustomUserPrincipal principal,
                                           @PathVariable Long itemId,
                                           Model model){

        if(userId != principal.getUser().getId()){
            throw new AccessDeniedException("해당 스케쥴에 접근 권한이 없습니다.");
        }

        //스케쥴 > 아이템 불러와서
        //해당 값들 모델에 전달 하고, create 뷰 리턴
        ScheduleItem scheduleItem = scheduleService.findScheduleItemByItemId(itemId);

        ScheduleItemDTO scheduleItemDTO = new ScheduleItemDTO(scheduleItem);

        model.addAttribute("userId", userId);
        model.addAttribute("schedule", scheduleService.findScheduleByScheduleId(scheduleId).orElseThrow());
        model.addAttribute("scheduleItemDTO", scheduleItemDTO);

        model.addAttribute("isNew", false);

        return "/schedule/schedule_item/schedule_item_create";
    }
}
