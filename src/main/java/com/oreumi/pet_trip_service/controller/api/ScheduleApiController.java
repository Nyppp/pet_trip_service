package com.oreumi.pet_trip_service.controller.api;

import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleItemDTO;
import com.oreumi.pet_trip_service.error.ErrorResponse;
import com.oreumi.pet_trip_service.model.Schedule;
import com.oreumi.pet_trip_service.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleService scheduleService;

    @Operation(summary = "모든 스케쥴 조회", description = "등록된 모든 스케쥴 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules(){
         List<ScheduleDTO> schedules = scheduleService.findAllSchedules();
         return ResponseEntity.ok(schedules);
    }

    @Operation(summary = "특정 유저의 모든 스케쥴 조회", description = "특정 유저의 등록된 모든 스케쥴 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/schedules")
    public ResponseEntity<List<ScheduleDTO>> getAllSchedulesByUserId(@PathVariable Long id){
        List<ScheduleDTO> schedules = scheduleService.findAllSchedulesByUserId(id);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping("/{id}/schedules")
    public ResponseEntity<Void> createSchedule(@PathVariable Long id,
                                              @RequestBody @Valid ScheduleDTO scheduleDTO,
                                              UriComponentsBuilder uriBuilder){
        Schedule schedule = scheduleService.saveSchedule(id, scheduleDTO);
        URI location = uriBuilder
                .path("/users/{id}/schedules/{scheduleId}")
                .buildAndExpand(id, schedule.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }


    @Operation(summary = "특정 스케쥴 내 모든 일정 조회", description = "스케쥴 내 모든 일정을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ScheduleItemDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/schedules/{scheduleId}/scheduleItems")
    public ResponseEntity<List<ScheduleItemDTO>> getAllItemsBySchedule(@PathVariable Long scheduleId){
        List<ScheduleItemDTO> scheduleItems = scheduleService.findAllScheduleItems(scheduleId);
        return ResponseEntity.ok(scheduleItems);
    }

    @Operation(summary = "특정 스케쥴 삭제", description = "특정 스케쥴을 삭제합니다. (하위 일정도 모두 삭제)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId){
        scheduleService.deleteSchdule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 스케쥴 일정 삭제", description = "특정 스케쥴내의 일정을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/schedules/{scheduleId}/items/{itemId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long scheduleId,
                                            @PathVariable Long itemId){
        scheduleService.deleteSchduleItem(scheduleId, itemId);
        return ResponseEntity.noContent().build();
    }
}
