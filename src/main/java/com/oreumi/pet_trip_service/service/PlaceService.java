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

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceImgRepository placeImgRepository;
    private final ChatService chatService;
    private final ReviewRepository reviewRepository;

    public List<PlaceDTO> findTop12ByOrderByRatingDesc(){
        List<Place> places = placeRepository.findTop12ByOrderByRatingDesc();
        
        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();

        List<PlaceImg> mainImgs = placeImgRepository.findMainImgsByPlaceIds(placeIds);

        Map<Long, String> mainImgMap = mainImgs.stream()
                .collect(Collectors.toMap(img -> img.getPlace().getId(),
                        PlaceImg::getUrl,
                        (a, b) -> a)); // 혹시 중복이면 첫 번째만

        List<PlaceDTO> dtoList = new ArrayList<>();
        for (Place place : places) {
            PlaceDTO dto = new PlaceDTO(place);

            String url = mainImgMap.get(place.getId());
            dto.getImageUrls().add(url);

            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<PlaceDTO> findAll(){
        List<Place> places = placeRepository.findAll();

        List<Long> placeIds = places.stream()
                .map(Place::getId)
                .toList();

        List<PlaceImg> mainImgs = placeImgRepository.findMainImgsByPlaceIds(placeIds);

        Map<Long, String> mainImgMap = mainImgs.stream()
                .collect(Collectors.toMap(img -> img.getPlace().getId(),
                        PlaceImg::getUrl,
                        (a, b) -> a));

        List<PlaceDTO> dtoList = new ArrayList<>();
        for (Place place : places) {
            PlaceDTO dto = new PlaceDTO(place);

            String url = mainImgMap.get(place.getId());
            dto.getImageUrls().add(url);

            dtoList.add(dto);
        }
        return dtoList;
    }

    public List<PlaceDTO> sortByRating(List<PlaceDTO> dto){
        Collections.sort(dto, new Comparator<PlaceDTO>() {
            @Override
            public int compare(PlaceDTO o1, PlaceDTO o2) {
                return Double.compare(o2.getRating(), o1.getRating());
            }
        });
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
            String reviewText = chatService.AlanAiReply(promptReview);
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
            String petText = chatService.AlanAiReply(promptPet);
            petText = normalizeAiText(petText);
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
        s = s.replace("\r\n", "\n").replace("\r", "\n").trim();

        String[] promptEcho = {
                "(?im)^\\s*장소명\\s*['\"].+?['\"].*$",
                "(?im)^\\s*[^\\n]*의\\s*(장점과\\s*단점|반려동물\\s*동반\\s*관련\\s*정보)[^\\n]*(정리해\\s*드리겠|정리해드리겠|정리해\\s*주세요|정리해줘)[^\\n]*$"
        };
        for (String p : promptEcho) s = s.replaceAll(p, "");

        String[] killLinePatterns = {
                "(?im)^.*정리해\\s*드리겠습니다\\.?\\s*$",
                "(?im)^.*정리해드리겠습니다\\.?\\s*$",
                "(?im)^.*정리해\\s*주세요\\.?\\s*$",
                "(?im)^.*도움이\\s*되(길|셨).*\\s*$",
                "(?im)^.*추가(적인)?\\s*질문.*\\s*$",
                "(?im)^.*언제든지\\s*말씀.*\\s*$",
                "(?im)^.*좋을\\s*것\\s*같습니다.*\\s*$",
                "(?im)^.*방문\\s*계획.*\\s*$",
                "(?im)^.*이\\s*정보(를|를\\s*바탕으로).*\\s*$"
        };
        for (String p : killLinePatterns) s = s.replaceAll(p, "");

        s = s.replaceAll("\\[(?:출처|source)\\s*\\d+\\]\\([^)]*\\)", "");
        s = s.replaceAll("\\(\\s*(?:출처|source)\\s*\\d+\\s*\\)", "");

        s = s.replaceAll("\\[\\s*([^\\]]*?)\\s*\\]\\([^)]*\\)", "$1");

        s = s.replaceAll("\\[\\s*\\]\\([^)]*\\)", ""); // [](...)
        s = s.replaceAll("\\[\\s*\\]\\s*\\(", "");     // [](

        s = s.replaceAll("https?://\\S+", "");

        s = s.replaceAll("\\(\\s*\\)", "");
        s = s.replaceAll("\\[\\s*\\]", "");

        s = s.replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "");
        s = s.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        s = s.replaceAll("__(.+?)__", "$1");
        s = s.replaceAll("\\*(?!\\s)([^*]+?)\\*", "$1");
        s = s.replaceAll("_(?!\\s)([^_]+?)_", "$1");

        s = s.replaceAll("(?im)^\\s*장점\\s*[:：]?\\s*$", "장점");
        s = s.replaceAll("(?im)^\\s*단점\\s*[:：]?\\s*$", "단점");
        s = s.replaceAll(
                "(?im)^\\s*(가능\\s*여부|크기/품종\\s*제한|리드줄[·•]\\s*입마개[·•]\\s*배변\\s*규정|리드줄·입마개·배변\\s*규정|구역\\s*제한|주의/준비물/팁)\\s*[:：]?\\s*$",
                "$1"
        );

        s = s.replaceAll("(?m)^\\s*\\d+[.)]\\s+", "• ");
        s = s.replaceAll("(?m)^\\s*[\\-–—]\\s+", "• ");
        s = s.replaceAll("(?m)^\\s*•\\s*•\\s*", "• ");

        s = s.replaceAll("(?m)^[ \\t]+$", "");
        s = s.replaceAll("\\n{3,}", "\n\n");
        s = s.replaceAll("[ \\t]+\\n", "\n");
        s = s.replaceAll("(?m)^단점\\s*$", "\n단점");

        return s.trim();
    }

}
