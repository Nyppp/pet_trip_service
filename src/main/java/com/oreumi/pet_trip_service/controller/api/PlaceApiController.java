package com.oreumi.pet_trip_service.controller.api;

import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaceApiController {
    private final PlaceRepository placeRepository;

    @GetMapping("/search")
    public List<Place> searchPlaces(@RequestParam("q") String keyword) {
        return placeRepository.findAllByNameContainingIgnoreCase(keyword);
    }
}
