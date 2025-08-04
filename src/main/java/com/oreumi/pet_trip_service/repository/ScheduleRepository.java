package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    public Optional<Schedule> findById(Long Id);
    public List<Schedule> findAllScheduleByUserId(Long userId);


}
