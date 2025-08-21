package com.oreumi.pet_trip_service.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "메시지 페이지 응답")
public record MessagePageDTO(
        @Schema(description = "메시지 목록") List<ChatDTO> items,
        @Schema(description = "다음 페이지 조회 커서", example = "1724038900000") Long nextCursor,
        @Schema(description = "추가 조회 가능 여부", example = "true") Boolean hasMore
) {
}
