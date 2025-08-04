package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByUserId(Long userId);
    List<Schedule> findAllByUserIdOrderByStartDate(Long userId);
}
