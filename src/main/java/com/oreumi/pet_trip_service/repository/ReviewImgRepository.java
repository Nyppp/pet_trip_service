package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {
    List<ReviewImg> findAllByReviewId(Long reviewId);
    void deleteAllByReviewId(Long reviewId);
}
