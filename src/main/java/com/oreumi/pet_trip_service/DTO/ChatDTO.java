package com.oreumi.pet_trip_service.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Schema(description = "채팅 메시지")
@Getter
@Setter
@RequiredArgsConstructor
public class ChatDTO {

    @Schema(description = "보낸이(이메일 또는 'chatbot')", example = "user@example.com")
    private String sender;

    @Schema(description = "메시지 본문", example = "안녕하세요!")
    private String message;

    @Schema(description = "보낸 시각", example = "2025-08-19T10:00:00")
    private LocalDateTime sendAt;

    public ChatDTO(String sender, String message, LocalDateTime sendAt) {
        this.sender = sender;
        this.message = message;
        this.sendAt = sendAt;
    }
}
