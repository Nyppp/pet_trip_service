package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Enum.Category;
import com.oreumi.pet_trip_service.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    private final PlaceService placeService;

    @GetMapping
    public String home(Model model) {
        List<PlaceDTO> placeDTO = placeService.findAllPlaces();

        model.addAttribute("category", Category.getCat1CodeDescriptionMap());
        model.addAttribute("places", placeDTO);


        return "main/main";
    }
}