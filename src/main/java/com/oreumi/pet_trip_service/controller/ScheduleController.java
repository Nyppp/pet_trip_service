package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.model.Place;
import lombok.Getter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    @GetMapping
    public String showScheduleList(){
        return "/schedule/schedule_list";
    }

    @GetMapping("/{id}")
    public String showScheduleDetail(@PathVariable("id") Long scheduleId,
                                     Model model){
        //서비스 구현 > id 접근

        return "/schedule/schedule_detail";
    }

    @GetMapping("/new")
    public String showScheduleForm(){
        
        //스케쥴 새로 만들기
        
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
    public String createNewSchedule(@RequestParam String title,
                                    @RequestParam LocalDate startDate,
                                    @RequestParam LocalDate endDate,
                                    Model model){

        //제목, 시작일, 종료일 정도만 우선 받고 > 스케쥴 서비스 > 저장
        //세션 or AuthenticationPrincipal 접근 > 유저 정보 불러와서 > 유저 리스트 > 해당 스케쥴 add 후 유저 정보 저장
        return "/schedule/schedule_list";
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


    @GetMapping("/{id}/items")
    public String showScheduleItemList(@PathVariable("id") Long scheduleId,
                                       Model model){
        //아이디로 스케쥴 접근 > 스케쥴 하위의 아이템 리스트 모델에 전달

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
