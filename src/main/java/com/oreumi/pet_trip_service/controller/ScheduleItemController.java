package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.service.ScheduleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.TreeMap;

@Controller
@RequestMapping("/schedule")
public class ScheduleItemController {
    private final ScheduleService scheduleService;

    public ScheduleItemController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping("/{id}/items")
    public String showScheduleItemList(@PathVariable("id") Long scheduleId,
                                       Model model){
        //아이디로 스케쥴 접근 > 스케쥴 하위의 아이템 리스트 모델에 전달
        Schedule schedule = scheduleService.findByScheduleId(scheduleId)
                .orElseThrow();

        Map<LocalDate, List<ScheduleItem>> groupedItems = schedule.getScheduleItems().stream()
                .collect(Collectors.groupingBy(
                        scheduleItem -> scheduleItem.getStartTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("schedule", schedule);
        model.addAttribute("scheduleGroup", groupedItems);

        return "/schedule/schedule_detail";
    }

    @GetMapping("/{id}/items/new")
    public String showScheduleItemForm(@PathVariable("id") Long scheduleId){
        return "/schedule/schedule_item/schedule_item_create";
    }


    @PostMapping("/{id}/items/new")
    public String createNewScheduleItem(@PathVariable("id") Long scheduleId,
                                        @RequestParam Long placeId,
                                        @RequestParam LocalDateTime startTime,
                                        @RequestParam LocalDateTime endTime,
                                        @RequestParam String memo,
                                        Model model){

        //파라미터들 받아서 스케쥴아이템 생성 후 > 스케쥴에 저장

        return "/schedule/schedule_item/schedule_item_detail";
    }

    @GetMapping("/{scheduleId}/items/{itemId}")
    public String showScheduleItemDetail(@PathVariable Long scheduleId,
                                         @PathVariable Long itemId,
                                         Model model){

        //스케쥴 > 아이템 불러와서

        return "/schedule/schedule_item/schedule_item_detail";

    }

    @GetMapping("/{scheduleId}/items/{itemId}/edit")
    public String showEditScheduleItemForm(@PathVariable Long scheduleId,
                                           @PathVariable Long itemId,
                                           Model model){

        //스케쥴 > 아이템 불러와서
        //해당 값들 모델에 전달 하고, create 뷰 리턴

        return "/schedule/schedule_item/schedule_item_create";

    }

    @PutMapping("/{scheduleId}/items/{itemId}/edit")
    public String editScheduleItem(@PathVariable Long scheduleId,
                                   @PathVariable Long itemId,
                                   @RequestParam Place place,
                                   @RequestParam LocalDateTime startTime,
                                   @RequestParam LocalDateTime endTime,
                                   @RequestParam String memo,
                                   Model model){

        //파라미터들 받아서 스케쥴아이템 생성 후 > 스케쥴에 저장
        //전달하면서, 스케쥴 id + 아이템 id 전달

        return "redirect:/schedule/{scheduleId}/items/{itemId}";
    }

    @DeleteMapping("/{scheduleId}/items/{itemId}")
    public String deleteScheduleItem(@PathVariable Long scheduleId){
        //해당 아이템 제거 후, 스케쥴 > 아이템 리스트로 리턴

        return "redirect:/schedule/{scheduleId}";
    }
}
