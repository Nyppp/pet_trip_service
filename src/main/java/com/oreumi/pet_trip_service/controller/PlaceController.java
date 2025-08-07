package com.oreumi.pet_trip_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlaceController {
    @GetMapping("/search")
    public String search() {
        return "place/search";
    }

    @GetMapping("/place")
    public String placeDetail() {
        return "place/place";
    }
}