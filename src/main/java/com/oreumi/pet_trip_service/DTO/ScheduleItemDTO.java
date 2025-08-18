package com.oreumi.pet_trip_service.DTO;

import com.oreumi.pet_trip_service.model.ScheduleItem;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long scheduleId;
    private Long placeId;

    @NotBlank
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
