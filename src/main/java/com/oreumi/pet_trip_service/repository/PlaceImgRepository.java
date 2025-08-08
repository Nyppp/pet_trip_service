package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceImgRepository extends JpaRepository<PlaceImg, Long> {
    void deleteByPlace(Place place);
    List<PlaceImg> findByPlace(Place place);
    Optional<PlaceImg> findFirstByPlaceIdAndMainImgTrue(Long PlaceId);
}
