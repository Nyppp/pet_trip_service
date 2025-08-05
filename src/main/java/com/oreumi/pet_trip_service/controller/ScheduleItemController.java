package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.service.ScheduleService;
import jakarta.validation.Valid;
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
@RequestMapping("/schedule")
public class ScheduleItemController {
    private final ScheduleService scheduleService;

    public ScheduleItemController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/{id}")
    public String showScheduleItemList(@PathVariable("id") Long scheduleId,
                                       Model model){
        //스케쥴 객체 로딩
        Schedule schedule = scheduleService.findScheduleByScheduleId(scheduleId)
                .orElseThrow();

        //스케쥴 (일차 - 스케쥴 아이템) 쌍으로 이루어진 맵을 받는다
        Map<LocalDate, List<ScheduleItem>> groupedItems = scheduleService.getScheduleItemsGroup(schedule);

        model.addAttribute("schedule", schedule);
        model.addAttribute("scheduleGroup", groupedItems);

        return "/schedule/schedule_detail";
    }

    @GetMapping("/{id}/items/new")
    public String showScheduleItemForm(@PathVariable("id") Long scheduleId,
                                       Model model){
        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("scheduleItemDTO", new ScheduleItemDTO());
        String formAction = String.format("/schedule/%d/items/new", scheduleId);
        return "/schedule/schedule_item/schedule_item_create";
    }


    @PostMapping("/{id}/items/new")
    public String createNewScheduleItem(@PathVariable("id") Long scheduleId,
                                        @Valid @ModelAttribute ScheduleItemDTO scheduleItemDTO,
                                        Model model){

        //파라미터들 받아서 스케쥴아이템 생성 후 > 스케쥴에 저장
        scheduleService.saveScheduleItem(scheduleId, scheduleItemDTO);

        return "redirect:/schedule/{id}";
    }

    @GetMapping("/{scheduleId}/items/{itemId}")
    public String showScheduleItemDetail(@PathVariable Long scheduleId,
                                         @PathVariable Long itemId,
                                         Model model){

        ScheduleItem scheduleItem = scheduleService.findScheduleItemByItemId(itemId).orElseThrow();
        model.addAttribute("item", scheduleItem);

        return "/schedule/schedule_item/schedule_item_detail";
    }

    @GetMapping("/{scheduleId}/items/{itemId}/edit")
    public String showEditScheduleItemForm(@PathVariable Long scheduleId,
                                           @PathVariable Long itemId,
                                           Model model){

        //스케쥴 > 아이템 불러와서
        //해당 값들 모델에 전달 하고, create 뷰 리턴
        ScheduleItem scheduleItem = scheduleService.findScheduleItemByItemId(itemId).orElseThrow();

        ScheduleItemDTO scheduleItemDTO = new ScheduleItemDTO(
                scheduleItem.getTitle(),
                scheduleItem.getStartTime(),
                scheduleItem.getEndTime(),
                scheduleItem.getMemo()
        );

        model.addAttribute("scheduleId", scheduleId);
        model.addAttribute("scheduleItemDTO", scheduleItemDTO);

        String formAction = String.format("/schedule/%d/items/%d/edit", scheduleId, itemId);
        model.addAttribute("formAction", formAction);

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
