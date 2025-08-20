package com.oreumi.pet_trip_service.error;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int code;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ErrorResponse(int value, String reasonPhrase, String message) {
        this.code = value;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.details = Map.of("error", reasonPhrase);
    }
}
