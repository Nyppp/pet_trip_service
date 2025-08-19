package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.model.ScheduleItem;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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
    private final PlaceRepository placeRepository;
    private final PlaceImgRepository placeImgRepository;


    public ScheduleService(ScheduleRepository scheduleRepository, ScheduleItemRepository scheduleItemRepository, UserRepository userRepository, PlaceRepository placeRepository, PlaceImgRepository placeImgRepository) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleItemRepository = scheduleItemRepository;
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.placeImgRepository = placeImgRepository;
    }


    @Transactional
    public Schedule saveSchedule(Long userId, ScheduleDTO scheduleDTO){
        if(!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("유저 정보를 찾을 수 없습니다.");
        }

        Schedule schedule = new Schedule();
        schedule.setTitle(scheduleDTO.getTitle());
        schedule.setStartDate(scheduleDTO.getStartDate());
        schedule.setEndDate(scheduleDTO.getEndDate());
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUser(userRepository.findById(userId).orElseThrow());

        return scheduleRepository.save(schedule);
    }

    @Transactional
    public ScheduleItem saveScheduleItem(Long userId, Long scheduleId, ScheduleItemDTO scheduleItemDTO){
        ScheduleItem scheduleItem = new ScheduleItem();
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow(() -> new IllegalArgumentException("유효한 일정 ID가 아닙니다."));

        if(scheduleItemDTO.getPlaceId() == null){
            throw new IllegalArgumentException("유효한 장소를 입력해주세요.");
        }

        if (!schedule.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("해당 일정에 접근할 수 없습니다.");
        }

        scheduleItem.setPlace(placeRepository.findById(scheduleItemDTO.getPlaceId()).orElseThrow());
        scheduleItem.setStartTime(scheduleItemDTO.getStartTime());
        scheduleItem.setEndTime(scheduleItemDTO.getEndTime());
        scheduleItem.setMemo(scheduleItemDTO.getMemo());
        scheduleItem.setCreatedAt(LocalDateTime.now());
        scheduleItem.setSchedule(schedule);

        schedule.getScheduleItems().add(scheduleItem);


        return scheduleItemRepository.save(scheduleItem);
    }

    @Transactional
    public ScheduleItem updateScheduleItem(Long scheduleId, Long scheduleItemId, ScheduleItemDTO scheduleItemDTO){
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleItemId).orElseThrow();
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();

        scheduleItem.setPlace(placeRepository.findById(scheduleItemDTO.getPlaceId()).orElseThrow());
        scheduleItem.setMemo(scheduleItemDTO.getMemo());
        scheduleItem.setStartTime(scheduleItemDTO.getStartTime());
        scheduleItem.setEndTime(scheduleItemDTO.getEndTime());

        return scheduleItemRepository.save(scheduleItem);
    }

    public Schedule updateSchedule(Long scheduleId, ScheduleDTO dto){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException());

        // 일정 정보 수정
        schedule.setTitle(dto.getTitle());
        schedule.setStartDate(dto.getStartDate());
        schedule.setEndDate(dto.getEndDate());

        schedule.getScheduleItems().removeIf(item ->
                item.getStartTime().toLocalDate().isBefore(dto.getStartDate()) ||
                        item.getEndTime().toLocalDate().isAfter(dto.getEndDate()));

        return scheduleRepository.save(schedule);
    }

    public void deleteSchdule(Long scheduleId){
        if(scheduleRepository.existsById(scheduleId)){
            scheduleRepository.deleteById(scheduleId);
        }else{
            throw new EntityNotFoundException("스케쥴 데이터를 찾을 수 없습니다.");
        }
    }

    public void deleteSchduleItem(Long scheduleId,Long scheduleItemId){
        if(!scheduleRepository.existsById(scheduleId)){
            throw new EntityNotFoundException("스케쥴 데이터를 찾을 수 없습니다.");
        }




        if(scheduleItemRepository.existsById(scheduleItemId)){
            scheduleItemRepository.deleteById(scheduleItemId);
        }else{
            throw new EntityNotFoundException("일정 데이터를 찾을 수 없습니다.");
        }
    }


    public List<ScheduleDTO> findAllSchedules(){

        List<Schedule> scheduleList = scheduleRepository.findAll();

        List<ScheduleDTO> scheduleDTOList = new ArrayList<>();

        for (Schedule schedule : scheduleList){
            ScheduleDTO scheduleDTO = new ScheduleDTO(
                    schedule.getId(),
                    schedule.getTitle(),
                    schedule.getStartDate(),
                    schedule.getEndDate()
            );
            scheduleDTOList.add(scheduleDTO);
        }

        return scheduleDTOList;
    }

    public List<ScheduleDTO> findAllSchedulesByUserId(Long userId){
        User user = userRepository.findById(userId).orElseThrow();
        List<Schedule> scheduleList = scheduleRepository.findAll();

        List<ScheduleDTO> scheduleDTOList = new ArrayList<>();

        for (Schedule schedule : scheduleList){
            if(schedule.getUser() != user){
                continue;
            }

            ScheduleDTO scheduleDTO = new ScheduleDTO(
                    schedule.getId(),
                    schedule.getTitle(),
                    schedule.getStartDate(),
                    schedule.getEndDate()
            );
            scheduleDTOList.add(scheduleDTO);
        }

        return scheduleDTOList;
    }


    public Optional<Schedule> findScheduleByScheduleId(Long id){
        return scheduleRepository.findById(id);
    }

    public Optional<ScheduleItem> findScheduleItemByItemId(Long id){
        return scheduleItemRepository.findById(id);
    }

    public List<ScheduleItemDTO> findAllScheduleItems(Long scheduleId){
        Schedule schedule = scheduleRepository.findById(scheduleId).orElseThrow();
        List<ScheduleItemDTO> scheduleItemDTOList = new ArrayList<>();

        for(ScheduleItem item : schedule.getScheduleItems()){
            ScheduleItemDTO dto = new ScheduleItemDTO(item);
            placeImgRepository.findMainImgUrlByScheduleItemId(item.getId())
                            .ifPresent(url -> dto.setPlaceImgUrl(url));

            scheduleItemDTOList.add(dto);
        }

        return scheduleItemDTOList;
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
