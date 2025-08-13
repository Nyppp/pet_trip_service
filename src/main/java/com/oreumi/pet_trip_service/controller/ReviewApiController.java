package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.ReviewDTO;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/reviews")
public class ReviewApiController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> create(@PathVariable Long placeId,
                                    @AuthenticationPrincipal CustomUserPrincipal principal,
                                    @Valid @RequestBody ReviewDTO dto) {
        if (principal == null) return ResponseEntity.status(401).build();
        ReviewDTO res = reviewService.create(principal.getUser().getId(), placeId, dto);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> list(@PathVariable Long placeId) {
        return ResponseEntity.ok(reviewService.listByPlace(placeId));
    }
}
