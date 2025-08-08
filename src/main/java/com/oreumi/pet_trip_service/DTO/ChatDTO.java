package com.oreumi.pet_trip_service.DTO;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class ChatDTO {

    public String sender;
    public String message;
    public LocalDateTime sendAt;

    public ChatDTO(String sender, String message, LocalDateTime sendAt) {
        this.sender = sender;
        this.message = message;
        this.sendAt = sendAt;
    }
}
