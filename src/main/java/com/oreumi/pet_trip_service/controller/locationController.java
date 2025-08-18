package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/location")
@RequiredArgsConstructor
public class locationController {
    private final PlaceService placeService;

    @Value("${google.maps.api-key}")
    private String apiKey;

    @Value("${google.maps.url}")
    private String apiURL;

    @GetMapping("/permission")
    public String showPermissionPage(){
        return "place/location_permission";
    }

    @GetMapping("/search")
    public String showLocationSearchPage(Model model){
        model.addAttribute("googleMapAPIKey", apiKey);
        model.addAttribute("googleMapAPIUrl", apiURL);

        model.addAttribute("places",placeService.findAllPlaces());
        return "place/location_search";
    }
}
