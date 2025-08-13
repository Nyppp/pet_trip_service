package com.oreumi.pet_trip_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDTO {
    private boolean success;
    private String message;
    private String imageUrl;
}
