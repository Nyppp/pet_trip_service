package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.LikeService;
import com.oreumi.pet_trip_service.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PlaceController {
    private final PlaceService placeService;
    private final LikeService likeService;

    @GetMapping("/search")
    public String search() {
        return "place/search";
    }

    @GetMapping("/place/{placeId}")
    public String placeDetail(@PathVariable Long placeId,
                              @AuthenticationPrincipal CustomUserPrincipal principal, // ✅ 로그인 사용자
                              Model model) {
        PlaceDTO place = placeService.getPlaceDetail(placeId);
        model.addAttribute("place", place);

        Long userId = null;
        boolean likedByMe = false;
        if (principal != null) {
            userId = principal.getUser().getId();
            likedByMe = likeService.isLiked(userId, placeId); // ✅ 내가 찜했는지
        }
        model.addAttribute("likedByMe", likedByMe); // ✅ 템플릿에서 사용
        model.addAttribute("userId", userId);

        return "place/place";
    }

    @PostMapping("/place/{placeId}/ai/summary")
    @ResponseBody
    public ResponseEntity<?> createOrRefreshAiSummary(
            @PathVariable Long placeId,
            @RequestParam(name = "force", defaultValue = "true") boolean force) {
        try {
            Place saved = placeService.generateAndSaveAiSummaries(placeId, force);
            return ResponseEntity.ok(Map.of(
                    "aiReview", Optional.ofNullable(saved.getAiReview()).orElse(""),
                    "aiPet",    Optional.ofNullable(saved.getAiPet()).orElse("")
            ));
        } catch (IllegalArgumentException e) { // place not found 등
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "AI 요약 업데이트 실패"));
        }
    }
}