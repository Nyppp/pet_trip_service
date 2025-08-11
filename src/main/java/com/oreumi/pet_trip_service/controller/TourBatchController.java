package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.service.api.PlaceBatchCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tour")
@RequiredArgsConstructor
public class TourBatchController {

    private final PlaceBatchCollector placeBatchCollector;

    @GetMapping("/collect-all")
    public String collectAll() throws Exception {
        placeBatchCollector.collectAll();
        return "전국 전체 지역 + 전체 콘텐츠타입 데이터 수집 완료";
    }
}