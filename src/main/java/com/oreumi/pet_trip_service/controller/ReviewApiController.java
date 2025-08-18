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

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> delete(@PathVariable Long placeId,
                                   @PathVariable Long reviewId,
                                   @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
        
        try {
            reviewService.deleteReview(reviewId, principal.getUser().getId());
            return ResponseEntity.ok().body("리뷰가 성공적으로 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("리뷰 삭제 중 오류가 발생했습니다.");
        }
    }
}
