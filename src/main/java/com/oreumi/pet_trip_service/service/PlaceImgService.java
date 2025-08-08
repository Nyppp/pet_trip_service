package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.repository.PlaceImgRepository;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import com.oreumi.pet_trip_service.repository.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceImgService {

    private final PlaceImgRepository placeImgRepository;
    private final PlaceRepository placeRepository;
    private final ScheduleItemRepository scheduleItemRepository;

    public String findMainImgUrlByScheduleItemId(Long scheduleItemId){
        String url = placeImgRepository.findMainImgUrlByScheduleItemId(scheduleItemId).orElseThrow();
        return url;
    }
}
