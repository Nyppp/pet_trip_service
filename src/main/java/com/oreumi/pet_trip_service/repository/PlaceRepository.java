package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Optional<Place> findByNameAndAddress(String name, String address);

    List<Place> findAllByNameContainingIgnoreCase(String keyword);
}