package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.model.Enum.AuthProvider;
import com.oreumi.pet_trip_service.model.Enum.UserStatus;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.ScheduleItemRepository;
import com.oreumi.pet_trip_service.repository.ScheduleRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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

    @Transactional
    public ScheduleItem saveScheduleItem(Long scheduleId, ScheduleItemDTO scheduleItemDTO){
        ScheduleItem scheduleItem = new ScheduleItem();
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();

        scheduleItem.setTitle(scheduleItemDTO.getTitle());
        scheduleItem.setStartTime(scheduleItemDTO.getStartTime());
        scheduleItem.setEndTime(scheduleItemDTO.getEndTime());
        scheduleItem.setMemo(scheduleItemDTO.getMemo());
        scheduleItem.setCreatedAt(LocalDateTime.now());
        scheduleItem.setSchedule(schedule);

        return scheduleItemRepository.save(scheduleItem);
    }


    public List<Schedule> findAllSchedules(){
        return scheduleRepository.findAll();
    }

    public Optional<Schedule> findByScheduleId(Long id){
        return scheduleRepository.findById(id);
    }

    public Map<LocalDate, List<ScheduleItem>> getScheduleItemsGroup(Schedule schedule){
        Map<LocalDate, List<ScheduleItem>> groupedItems = schedule.getScheduleItems().stream()
                .collect(Collectors.groupingBy(
                        item -> item.getStartTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        LocalDate start = schedule.getStartDate();
        LocalDate end = schedule.getEndDate();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            groupedItems.putIfAbsent(date, new ArrayList<>());
        }

        return groupedItems;
    }
}
