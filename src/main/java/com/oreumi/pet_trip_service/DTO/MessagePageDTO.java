package com.oreumi.pet_trip_service.DTO;

import java.util.List;

public record MessagePageDTO(
        List<ChatDTO> items,
        Long nextCursor,
        Boolean hasMore
) {
}
