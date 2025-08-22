package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.ReviewImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImgRepository extends JpaRepository<ReviewImg, Long> {
    List<ReviewImg> findAllByReviewId(Long reviewId);
    List<ReviewImg> findAllByReviewIdOrderById(Long reviewId);
    void deleteAllByReviewId(Long reviewId);
    
    @Query("SELECT ri FROM ReviewImg ri WHERE ri.imgURL = :imgURL")
    List<ReviewImg> findByImgURL(@Param("imgURL") String imgURL);
}
