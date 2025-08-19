package com.oreumi.pet_trip_service.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {

    private Long id;

    private Long userId;

    private Long placeId;
    
    private String placeName;

    @NotNull @DecimalMin("0.5") @DecimalMax("5.0")
    private Double rating;

    @Size(max = 1000)
    private String content;

    private LocalDateTime createdAt;

    private List<String> images;

    @NotNull @Size(min = 1, max = 5)
    @Valid
    private List<PetInfoDTO> petInfos;

    private String nickname;
    private String userProfileImageUrl;
}
