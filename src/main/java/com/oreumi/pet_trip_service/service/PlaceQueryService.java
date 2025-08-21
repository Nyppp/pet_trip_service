// src/main/java/com/oreumi/pet_trip_service/service/PlaceQueryService.java
package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import com.oreumi.pet_trip_service.repository.PlaceImgRepository;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {
    private final PlaceRepository repo;
    private final PlaceImgRepository imgRepo;

    public Page<PlaceDTO> search(String cat1, String keyword, int page, int size, String sort) {
        String cat1Like = (cat1 == null || cat1.isBlank()) ? null : cat1.trim() + "%";
        String kwLike   = (keyword == null || keyword.isBlank()) ? null : ("%" + keyword.trim().toLowerCase() + "%");

        Page<Place> places = switch (sort == null ? "relevance" : sort.toLowerCase()) {
            case "rating" -> repo.searchFiltered(cat1Like, kwLike,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "id"))));
            case "liked" -> repo.searchFiltered(cat1Like, kwLike,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "liked").and(Sort.by(Sort.Direction.DESC, "id"))));
            default -> repo.searchByRelevance(cat1Like, kwLike, PageRequest.of(page, size));
        };

        return places.map(p -> {
            PlaceDTO dto = new PlaceDTO(p);
            imgRepo.findFirstByPlaceIdAndMainImgTrue(p.getId())
                    .or(() -> imgRepo.findByPlace(p).stream().findFirst())
                    .map(PlaceImg::getUrl)
                    .ifPresent(u -> dto.getImageUrls().add(u));
            return dto;
        });
    }
}
