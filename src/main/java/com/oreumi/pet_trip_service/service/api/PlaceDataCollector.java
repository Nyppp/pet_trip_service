package com.oreumi.pet_trip_service.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreumi.pet_trip_service.DTO.api.AreaBasedListResponse;
import com.oreumi.pet_trip_service.DTO.api.DetailCommonResponse;
import com.oreumi.pet_trip_service.DTO.api.DetailImageResponse;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import com.oreumi.pet_trip_service.repository.PlaceImgRepository;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.oreumi.pet_trip_service.model.Enum.Category;

import java.util.*;

@Service
@RequiredArgsConstructor
public class PlaceDataCollector {

    private final TourApiService tourApiService;
    private final PlaceRepository placeRepository;
    private final PlaceImgRepository placeImgRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void collectPlaces(int areaCode, int contentTypeId) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("areaCode", String.valueOf(areaCode));
        params.put("contentTypeId", String.valueOf(contentTypeId));
        params.put("numOfRows", "1000");
        params.put("pageNo", "1");
        params.put("_type", "json");

        // 1. 목록 요청
        String areaListJson = tourApiService.callApi("/areaBasedList", params);
        AreaBasedListResponse areaResponse;
        try {
            areaResponse = objectMapper.readValue(areaListJson, AreaBasedListResponse.class);
        } catch (JsonProcessingException e) {
            System.out.println("areaBasedList 비어있음");
            return;
        }
        List<AreaBasedListResponse.Item> items = areaResponse.getResponse().getBody().getItems().getItem();

        for (AreaBasedListResponse.Item item : items) {
            long contentId = item.getContentid();
            // 2. 상세정보 요청
            Map<String, String> detailParams = new HashMap<>();
            detailParams.put("contentId", String.valueOf(contentId));
            detailParams.put("overviewYN", "Y");
            detailParams.put("defaultYN", "Y");
            detailParams.put("_type", "json");

            String detailJson = tourApiService.callApi("/detailCommon", detailParams);
            DetailCommonResponse detailResponse;

            try {
                detailResponse = objectMapper.readValue(detailJson, DetailCommonResponse.class);
            } catch (JsonProcessingException e) {
                System.out.println("detailCommon 비어있음");
                continue;
            }
            List<DetailCommonResponse.Item> item2 = detailResponse.getResponse()
                    .getBody()
                    .getItems()
                    .getItem();

            String overview = null;
            String homepage = null;
            if (item2 != null && !item2.isEmpty()) {
                overview = item2.get(0).getOverview();
                homepage = item2.get(0).getHomepage();
            }
            Place place = placeRepository.findByNameAndAddress(item.getTitle(), item.getAddr1())
                    .orElse(new Place());

            place.setName(item.getTitle());
            place.setAddress(item.getAddr1());
            place.setDescription(overview);
            place.setCategoryCode(item.getCat3());
            String categoryName = null;
            try {
                categoryName = Category.valueOf(item.getCat3()).getDescription();
                place.setCategoryName(categoryName);
            } catch (IllegalArgumentException e) {
                place.setCategoryName("기타");
            }
            place.setCategoryName(categoryName);
            place.setLat(item.getMapy().isEmpty() ? 0.0 : Double.parseDouble(item.getMapy()));
            place.setLng(item.getMapx().isEmpty() ? 0.0 : Double.parseDouble(item.getMapx()));
            place.setPhone(item.getTel());
            place.setHomepageUrl(extractFirstUrl(homepage));

            Place savedPlace = placeRepository.save(place);

            // 4. 이미지 저장(대표이미지)
            if (item.getFirstimage() != null && !item.getFirstimage().isEmpty()) {
                PlaceImg img = new PlaceImg();
                img.setPlace(savedPlace);
                img.setUrl(item.getFirstimage());
                img.setMainImg(true);
                placeImgRepository.save(img);
            }

            // 5. 나머지 이미지 저장
            Map<String, String> imageParams = new HashMap<>();
            imageParams.put("contentId", String.valueOf(contentId));
            imageParams.put("imageYN", "Y");
            imageParams.put("_type", "json");

            String imageJson = tourApiService.callApi("/detailImage", imageParams);
            DetailImageResponse imageResponse;

            try {
                imageResponse = objectMapper.readValue(imageJson, DetailImageResponse.class);
            } catch (JsonProcessingException e) {
                System.out.println("detailImage 비어있음");
                continue;
            }
            List<DetailImageResponse.Item> imageItems = Optional.ofNullable(imageResponse)
                    .map(DetailImageResponse::getResponse)
                    .map(DetailImageResponse.Response::getBody)
                    .map(DetailImageResponse.Body::getItems)
                    .map(DetailImageResponse.Items::getItem)
                    .orElse(Collections.emptyList());

            for (DetailImageResponse.Item item3 : imageItems) {
                PlaceImg placeImg = new PlaceImg();
                placeImg.setPlace(savedPlace);
                placeImg.setUrl(item3.getOriginimgurl());
                placeImgRepository.save(placeImg);
            }

            Thread.sleep(100);
        }
    }
    private String extractFirstUrl(String raw) {
        if (raw == null || raw.isBlank()) return null;

        // http:// 또는 https:// 로 시작하는 URL 추출
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(https?://[^\\s\"<>]+)")
                .matcher(raw);

        if (matcher.find()) {
            return matcher.group(1); // 첫 번째 URL 반환
        }
        return null;
    }
}
