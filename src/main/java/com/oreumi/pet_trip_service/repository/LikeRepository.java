package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    List<Like> findAllByUserId(Long userId);

    Optional<Like> findByUserIdAndPlaceId(Long userId, Long PlaceId);
}
