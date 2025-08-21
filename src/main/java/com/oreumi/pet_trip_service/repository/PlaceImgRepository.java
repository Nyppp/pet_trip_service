package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceImgRepository extends JpaRepository<PlaceImg, Long> {
    void deleteByPlace(Place place);
    List<PlaceImg> findByPlace(Place place);
    Optional<PlaceImg> findFirstByPlaceIdAndMainImgTrue(Long PlaceId);

    @Query("""
    SELECT pi.url
    FROM ScheduleItem si
    JOIN si.place p
    JOIN PlaceImg pi ON pi.place = p AND pi.mainImg = true
    WHERE si.id = :scheduleItemId
    """)
    Optional<String> findMainImgUrlByScheduleItemId(@Param("scheduleItemId") Long scheduleItemId);

    @Query("""
    SELECT pi
    FROM PlaceImg pi
    WHERE pi.mainImg = true
      AND pi.place.id IN :placeIds
""")
    List<PlaceImg> findMainImgsByPlaceIds(@Param("placeIds") List<Long> placeIds);
}
