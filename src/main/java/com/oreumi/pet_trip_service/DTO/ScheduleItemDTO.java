package com.oreumi.pet_trip_service.DTO;

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
    @NotBlank
    private String title;

    @FutureOrPresent
    private LocalDateTime startTime;

    @Future
    private LocalDateTime endTime;

    private String memo;
}
