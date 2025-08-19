package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Enum.Category;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal; // 경로는 프로젝트에 맞게
import com.oreumi.pet_trip_service.service.LikeService;         // isLiked(userId, placeId) 사용
import com.oreumi.pet_trip_service.service.PlaceQueryService;   // Page<PlaceDTO> search(...) 반환
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final PlaceQueryService placeQueryService;
    private final LikeService likeService;

    @GetMapping
    public String search(@RequestParam(required = false) String cat1,
                         @RequestParam(required = false) String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "12") int size,
                         @RequestParam(defaultValue = "relevance") String sort,
                         @AuthenticationPrincipal CustomUserPrincipal principal,
                         Model model) {

        Page<PlaceDTO> result = placeQueryService.search(cat1, keyword, page, size, sort);

        Long userId = null;
        Map<Long, Boolean> likedByMeMap = new HashMap<>();
        if (principal != null) {
            userId = principal.getUser().getId();
            for (PlaceDTO dto : result.getContent()) {
                boolean liked = likeService.isLiked(userId, dto.getId());
                likedByMeMap.put(dto.getId(), liked);
            }
        }

        model.addAttribute("results", result.getContent());
        model.addAttribute("total", result.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("cat1", cat1);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        model.addAttribute("userId", userId);
        model.addAttribute("likedByMeMap", likedByMeMap);
        model.addAttribute("category", Category.getCat1CodeDescriptionMap());

        return "place/search";
    }

}
