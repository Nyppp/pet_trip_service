package com.oreumi.pet_trip_service.controller.api;
import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.error.ErrorResponse;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleItemApiController {
    private final ScheduleService scheduleService;


    @Operation(summary = "특정 스케쥴 내 모든 일정 조회", description = "스케쥴 내 모든 일정을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleItemDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/users/{userId}/schedules/{scheduleId}/items")
    public ResponseEntity<List<ScheduleItemDTO>> getAllItemsBySchedule(@PathVariable Long userId,
                                                                       @PathVariable Long scheduleId){
        List<ScheduleItemDTO> scheduleItems = scheduleService.findAllScheduleItems(scheduleId);
        return ResponseEntity.ok(scheduleItems);
    }

    @Operation(summary = "스케쥴 일정 생성", description = "스케쥴 ID 기준으로 일정을 생성합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "생성 성공",
                    headers = {
                            @Header(
                                    name = "Location",
                                    description = "생성된 스케줄 리소스의 URI",
                                    schema = @Schema(type = "string", example = "/users/42/schedules/101")
                            )
                    },
                    content = @Content // 바디 없음
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/users/{userId}/schedules/{scheduleId}/items")
    public ResponseEntity<Void> createSchedule(@PathVariable Long userId,
                                               @PathVariable Long scheduleId,
                                               @RequestBody @Valid ScheduleItemDTO scheduleItemDTO,
                                               UriComponentsBuilder uriBuilder){
        scheduleService.saveScheduleItem(scheduleId, scheduleItemDTO);
        URI location = uriBuilder
                .path("/users/{userId}/schedules/{scheduleId}")
                .build()
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
