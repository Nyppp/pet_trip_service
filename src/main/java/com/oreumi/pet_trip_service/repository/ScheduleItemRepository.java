package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findAllByScheduleId(Long scheduleId);
    List<ScheduleItem> findAllByScheduleIdOrderByStartTime(Long scheduleId);
}
