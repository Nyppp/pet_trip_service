package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.model.Enum.AuthProvider;
import com.oreumi.pet_trip_service.model.Enum.UserStatus;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.ScheduleItemRepository;
import com.oreumi.pet_trip_service.repository.ScheduleRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final UserRepository userRepository;


    public ScheduleService(ScheduleRepository scheduleRepository, ScheduleItemRepository scheduleItemRepository, UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleItemRepository = scheduleItemRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public Schedule saveSchedule(ScheduleDTO scheduleDTO){
        Schedule schedule = new Schedule();

        schedule.setTitle(scheduleDTO.getTitle());
        schedule.setStartDate(scheduleDTO.getStartDate());
        schedule.setEndDate(scheduleDTO.getEndDate());
        schedule.setCreatedAt(LocalDateTime.now());

        //임시 코드 (유저 정보 관련 없이, 우선 스케쥴만 등록 처리)
        //개발 좀 되고나서 반드시 지우도록!!!!!!!!!!!
        if(!userRepository.existsById(1L)) {
            User dummyUser = new User(); //더미용 유저 데이터 생성
            dummyUser.setEmail("testuser@example.com");
            dummyUser.setPassword("encoded_dummy_password");
            dummyUser.setNickname("여행왕초보");
            dummyUser.setStatus(UserStatus.ACTIVE);
            dummyUser.setProvider(AuthProvider.LOCAL);
            dummyUser.setProviderId(null);
            dummyUser.setProfileImg(null);

            userRepository.save(dummyUser);
        }

        schedule.setUser(userRepository.findById(1L).orElseThrow());

        return scheduleRepository.save(schedule);
    }

    public List<Schedule> findAllSchedules(){
        return scheduleRepository.findAll();
    }
}
