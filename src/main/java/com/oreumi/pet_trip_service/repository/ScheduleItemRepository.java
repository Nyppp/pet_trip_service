package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    List<ScheduleItem> findAllByScheduleId(Long scheduleId);
    List<ScheduleItem> findAllByScheduleIdOrderByStartTime(Long scheduleId);
}
