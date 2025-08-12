package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.service.PlaceImgService;
import com.oreumi.pet_trip_service.service.ScheduleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @GetMapping("/{scheduleId}")
    public String showScheduleItemList(@PathVariable Long userId,
                                       @PathVariable Long scheduleId,
                                         Model model){

        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("스케쥴을 찾을 수 없습니다."));
        model.addAttribute("userId", userId);
        model.addAttribute("schedule" , schedule);

        return "/schedule/schedule_detail";
    }

    @GetMapping("/{scheduleId}/items/new")
    public String showScheduleItemForm(@PathVariable Long userId,
                                       @PathVariable Long scheduleId,
                                       Model model){
        model.addAttribute("schedule", scheduleService.findScheduleByScheduleId(scheduleId).orElseThrow());
        model.addAttribute("scheduleItemDTO", new ScheduleItemDTO());
        model.addAttribute("userId", userId);
        model.addAttribute("isNew", true);
        return "/schedule/schedule_item/schedule_item_create";
    }

    @GetMapping("/{scheduleId}/items/{itemId}")
    public String showScheduleItemDetail(){
        return "/schedule/schedule_item/schedule_item_detail";
    }

    @GetMapping("/{scheduleId}/items/{itemId}/edit")
    public String showEditScheduleItemForm(@PathVariable Long userId,
                                           @PathVariable Long scheduleId,
                                           @PathVariable Long itemId,
                                           Model model){

        //스케쥴 > 아이템 불러와서
        //해당 값들 모델에 전달 하고, create 뷰 리턴
        ScheduleItem scheduleItem = scheduleService.findScheduleItemByItemId(itemId).orElseThrow();

        ScheduleItemDTO scheduleItemDTO = new ScheduleItemDTO(scheduleItem);

        model.addAttribute("userId", userId);
        model.addAttribute("schedule", scheduleService.findScheduleByScheduleId(scheduleId).orElseThrow());
        model.addAttribute("scheduleItemDTO", scheduleItemDTO);

        model.addAttribute("isNew", false);

        return "/schedule/schedule_item/schedule_item_create";

    }

    @PostMapping("/{scheduleId}/items/{itemId}/edit")
    public String editScheduleItem(@PathVariable Long scheduleId,
                                   @PathVariable Long itemId,
                                   @Valid @ModelAttribute ScheduleItemDTO scheduleItemDTO,
                                   Model model){

        //파라미터들 받아서 스케쥴아이템 생성 후 > 스케쥴에 저장
        //전달하면서, 스케쥴 id + 아이템 id 전달
        scheduleService.updateScheduleItem(scheduleId, itemId, scheduleItemDTO);

        return "redirect:/schedule/{scheduleId}";
    }

    @DeleteMapping("/{scheduleId}/items/{itemId}")
    public String deleteScheduleItem(@PathVariable Long scheduleId){
        //해당 아이템 제거 후, 스케쥴 > 아이템 리스트로 리턴

        return "redirect:/schedule/{scheduleId}";
    }
}
