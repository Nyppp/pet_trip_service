package com.oreumi.pet_trip_service.DTO;

import com.oreumi.pet_trip_service.model.ScheduleItem;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleItemDTO {
    private Long id;
    private Long scheduleId;
    private Long placeId;
    private String placeName;
    private String placeImgUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String memo;

    public ScheduleItemDTO(ScheduleItem item){
        this.id = item.getId();
        this.scheduleId = item.getSchedule().getId();
        this.placeId = item.getPlace().getId();
        this.placeName = item.getPlace().getName();
        this.startTime = item.getStartTime();
        this.endTime = item.getEndTime();
        this.memo = item.getMemo();
    }
}
