package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.PlaceDto;
import com.oreumi.pet_trip_service.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

@Controller
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;

    @GetMapping("/search")
    public String search() {
        return "place/search";
    }

    @GetMapping("/place/{placeId}")
    public String placeDetail(@PathVariable Long placeId, Model model) {
        PlaceDto place = placeService.getPlaceDetail(placeId);
        model.addAttribute("place", place);
        return "place/place";
    }
}