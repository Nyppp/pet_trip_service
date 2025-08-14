package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import com.oreumi.pet_trip_service.repository.PlaceImgRepository;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import com.oreumi.pet_trip_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceImgRepository placeImgRepository;
    private final ChatService chatService;
    private final ReviewRepository reviewRepository;

    public List<PlaceDTO> findAllPlaces(){
        List<PlaceDTO> dto = new ArrayList<>();


        List<Place> places = placeRepository.findAll();
        for (Place place : places){
            PlaceDTO placeDTO = new PlaceDTO(place);
            placeImgRepository.findFirstByPlaceIdAndMainImgTrue(placeDTO.getId())
                    .map(placeImg -> {
                        return placeDTO.getImageUrls().add(placeImg.getUrl());
                    });


            dto.add(placeDTO);
        }
        return dto;
    }

    public PlaceDTO getPlaceDetail(Long placeId) {
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. ID: " + placeId));

        List<String> imageUrls = placeImgRepository.findByPlace(place).stream()
                .map(PlaceImg::getUrl)
                .collect(Collectors.toList());
        long rc = reviewRepository.countByPlaceId(placeId);

        PlaceDTO dto = new PlaceDTO(
                place.getId(),
                place.getName(),
                place.getDescription(),
                place.getCategoryCode(),
                place.getCategoryName(),
                place.getAddress(),
                place.getLat(),
                place.getLng(),
                place.getPhone(),
                place.getRating(),
                place.getLiked(),
                place.getHomepageUrl(),
                place.getAiReview(),
                place.getAiPet(),
                imageUrls,
                rc
        );

        dto.setReviewCount(reviewRepository.countByPlaceId(placeId));

        return dto;
    }

    @Transactional
    public Place generateAndSaveAiSummaries(Long placeId, boolean force) {
        Place p = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("place not found: " + placeId));

        boolean changed = false;


        if (force || isBlank(p.getAiReview())) {
            String promptReview = """
            장소명 '%s'의 장점과 단점을 간단히 정리해줘.
            """.formatted(n(p.getName()));
            System.out.println(promptReview);
            String reviewText = chatService.AlanAiReply(promptReview);
            System.out.println(reviewText);
            reviewText = normalizeAiText(reviewText);
            if (isValidAiText(reviewText)) {
                p.setAiReview(cut(reviewText, 8000));
                changed = true;
            }
        }


        if (force || isBlank(p.getAiPet())) {
            String promptPet = """
            장소명 '%s'의 '반려동물 동반 관련 정보'만 정리해줘.
            (가능 여부, 크기/품종 제한, 리드줄·입마개·배변 규정, 구역 제한, 주의/준비물/팁 등)
            """.formatted(n(p.getName()));
            System.out.println(promptPet);
            String petText = chatService.AlanAiReply(promptPet);
            petText = normalizeAiText(petText);
            System.out.println(petText);
            if (isValidAiText(petText)) {
                p.setAiPet(cut(petText, 8000));
                changed = true;
            }
        }

        return changed ? placeRepository.save(p) : p;
    }

    private static String n(String s){ return s == null ? "" : s; }
    private static boolean isBlank(String s){ return s == null || s.isBlank(); }
    private static String cut(String s, int max){
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
    private static boolean isErrorResponse(String text) {
        if (text == null) return true;
        String lower = text.toLowerCase();
        return lower.contains("ai 응답 오류")
                || lower.contains("error")
                || lower.contains("exception")
                || lower.contains("invalid request")
                || lower.contains("rate limit")
                || lower.contains("fail")
                || lower.contains("not available");
    }
    private static boolean isValidAiText(String text) {
        return !isBlank(text) && !isErrorResponse(text);
    }

    private static String normalizeAiText(String s) {
        if (s == null) return "";
        s = s.replaceAll("(?m)^.*정리해 드리겠습니다\\.?\\s*$", "");
        s = s.replaceAll("(?m)^.*도움이 되길 바랍니다\\.?\\s*$", "");
        s = s.replaceAll("(?m)^.*추가적인 질문.*$", "");
        s = s.replaceAll("(?m)^.*질문!*$", "");


        s = s.replaceAll("\\[(?:출처|source)\\d+\\]\\([^)]*\\)", "");

        s = s.replaceAll("\\[([^\\]]+)\\]\\([^)]*\\)", "");

        s = s.replaceAll("(?m)^\\s*#{1,6}\\s*", "");     // 라인 앞의 ### 제거
        s = s.replaceAll("\\*\\*(.*?)\\*\\*", "$1");     // **text** → text
        s = s.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)\\*(?<!\\*)", "$1"); // *text* → text

        s = s.replaceAll("(?i)장점\\s*[:：]?\\s*", "장점\n");
        s = s.replaceAll("(?i)단점\\s*[:：]?\\s*", "단점\n");

        s = s.replaceAll("(?m)^\\s*\\d+\\.\\s+", "• ");
        s = s.replaceAll("(?m)^\\s*[\\-–]\\s+", "• ");

        return s;
    }
}
