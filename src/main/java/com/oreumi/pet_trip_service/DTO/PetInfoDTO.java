package com.oreumi.pet_trip_service.DTO;

import com.oreumi.pet_trip_service.model.Enum.PetType; // 개/고양이/기타
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetInfoDTO {

    @NotNull
    private PetType type;

    @Size(max = 50)
    private String breed;

    @NotNull
    @DecimalMin("0.1") @DecimalMax("100.0")
    private Double weightKg;
}
