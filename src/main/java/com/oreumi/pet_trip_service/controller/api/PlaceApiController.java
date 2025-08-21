package com.oreumi.pet_trip_service.controller.api;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import com.oreumi.pet_trip_service.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final PlaceService placeService;

    @GetMapping("/search")
    public List<Place> searchPlaces(@RequestParam("q") String keyword) {
        return placeRepository.findAllByNameContainingIgnoreCase(keyword);
    }

    @GetMapping("/places/all")
    public ResponseEntity<List<PlaceDTO>> findAllPlaces(){
        List<PlaceDTO> places =  placeService.findAll();
        return ResponseEntity.ok(places);
    }

    @GetMapping("/places/rating")
    public ResponseEntity<List<PlaceDTO>> findAllPlacesOrderByRatingTop12(){
        List<PlaceDTO> places =  placeService.findTop12ByOrderByRatingDesc();
        return ResponseEntity.ok(places);
    }
}
