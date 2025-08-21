package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/{placeId}/like")
public class LikeApiController {

    private final LikeService likeService;

    @GetMapping
    public ResponseEntity<?> status(@PathVariable Long placeId,
                                    @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        boolean liked = likeService.isLiked(principal.getUser().getId(), placeId);
        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "count", liked ? likeService.like(principal.getUser().getId(), placeId) - 0
                        : likeService.unlike(principal.getUser().getId(), placeId) + 0 // 상태만 알기용
        ));
    }

    @PostMapping
    public ResponseEntity<?> like(@PathVariable Long placeId,
                                  @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        int count = likeService.like(principal.getUser().getId(), placeId);
        return ResponseEntity.ok(Map.of("liked", true, "count", count));
    }

    @DeleteMapping
    public ResponseEntity<?> unlike(@PathVariable Long placeId,
                                    @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        int count = likeService.unlike(principal.getUser().getId(), placeId);
        return ResponseEntity.ok(Map.of("liked", false, "count", count));
    }
}
