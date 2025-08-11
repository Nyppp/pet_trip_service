package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.PlaceDto;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import com.oreumi.pet_trip_service.repository.PlaceImgRepository;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final PlaceImgRepository placeImgRepository;

    public PlaceDto getPlaceDetail(Long placeId) {
        // 1. 장소 조회
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 장소를 찾을 수 없습니다. ID: " + placeId));

        // 2. 해당 장소의 이미지 리스트 조회
        List<String> imageUrls = placeImgRepository.findByPlace(place).stream()
                .map(PlaceImg::getUrl)
                .collect(Collectors.toList());

        // 3. DTO 변환
        return new PlaceDto(
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
                imageUrls
        );
    }
}
