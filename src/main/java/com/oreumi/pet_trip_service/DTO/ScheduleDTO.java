package com.oreumi.pet_trip_service.DTO;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleDTO {
    private Long id;
    @NotBlank
    private String title;
    @FutureOrPresent
    private LocalDate startDate;
    @Future
    private LocalDate endDate;
}
