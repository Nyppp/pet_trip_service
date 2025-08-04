package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserId(Long userId);
    List<Review> findAllByUserIdOrderByCreatedAt(Long userId);

    List<Review> findAllByPlaceId(Long placeId);
    List<Review> findAllByPlaceIdOrderByCreatedAt(Long placeId);
    List<Review> findAllByPlaceIdOrderByRating(Long placeId);
}
