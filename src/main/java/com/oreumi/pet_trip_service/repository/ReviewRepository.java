package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserId(Long userId);
    List<Review> findAllByUserIdOrderByCreatedAt(Long userId);

    List<Review> findAllByPlaceId(Long placeId);
    List<Review> findAllByPlaceIdOrderByCreatedAt(Long placeId);
    List<Review> findAllByPlaceIdOrderByCreatedAtDesc(Long placeId);
    List<Review> findAllByPlaceIdOrderByRating(Long placeId);

    @Query("select avg(r.rating) from Review r where r.place.id = :placeId")
    Double findAverageRatingByPlaceId(@Param("placeId") Long placeId);
    boolean existsByUserIdAndPlaceId(Long userId, Long placeId);
    long countByPlaceId(Long placeId);
}
