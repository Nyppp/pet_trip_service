package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.DTO.MessagePageDTO;
import com.oreumi.pet_trip_service.DTO.ScheduleDTO;
import com.oreumi.pet_trip_service.error.ErrorResponse;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.UserRepository;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "chatController", description = "채팅방 및 메세지 API")
@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatController {


    private final ChatService chatService;
    private final UserRepository userRepository;

    @Operation(summary = "내 채팅방 ID 조회", description = "로그인한 사용자의 채팅방이 없으면 생성 후 ID를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"roomId\": 1}"))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"error\":\"로그인이 필요합니다.\",\"loginUrl\":\"/login\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<?> myChatRoomId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다.", "loginUrl", "/login"));
        }

        String email = chatService.extractEmail(authentication);
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        ChatRoom room = chatService.getOrCreateChatRoomForUser(me.getId());
        return ResponseEntity.ok(Map.of("roomId", room.getId()));
    }


    @Operation(summary = "채팅방 생성", description = "주어진 사용자 ID로 채팅방을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공",
                    content = @Content(schema = @Schema(implementation = Long.class),
                            examples = @ExampleObject(value = "1"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<Long> chatRoomCreated(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "채팅방 생성 요청. sender 필드에 사용자 ID를 문자열로 전달",
                    content = @Content(schema = @Schema(implementation = ChatDTO.class),
                            examples = @ExampleObject(value = "{\"sender\":\"1\",\"message\":\"\",\"createdAt\":\"2025-08-19T10:00:00\"}")
                    )
            )
            @RequestBody ChatDTO chatRequest
    ) {
        Long userId = Long.parseLong(chatRequest.getSender());
        ChatRoom room = chatService.createChatRoomForUser(userId);
        return ResponseEntity.ok(room.getId());
    }

    @Operation(
            summary = "채팅 메시지 페이지 조회",
            description = "cursor(이전 페이지의 nextCursor)를 사용해 과거 메시지를 페이지네이션 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = MessagePageDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<MessagePageDTO> getMessages(
            @Parameter(name="roomId", in= ParameterIn.PATH, required=true,
                    description="채팅방 ID", example="1")
                    @PathVariable Long roomId,
            @Parameter(name="cursor", in=ParameterIn.QUERY, required=false,
                    description="다음 페이지 조회 커서(이전 응답의 nextCursor)", example="1724038900000")
                    @RequestParam(required = false) Long cursor,
            @Parameter(name="size", in=ParameterIn.QUERY, required=false,
                    description="페이지 크기", example="30")
                    @RequestParam(defaultValue = "30") int size,
            Authentication authentication) {

        MessagePageDTO dto = chatService.getMessages(roomId, cursor, size);
        return ResponseEntity.ok(dto);
    }

    @Operation(
            summary = "채팅 메시지 생성",
            description = "사용자 메시지를 저장하고, 챗봇 응답을 생성하여 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 성공 (챗봇 응답이 바디로 반환)",
                    content = @Content(schema = @Schema(implementation = ChatDTO.class),
                            examples = @ExampleObject(value = "{\"sender\":\"chatbot\",\"message\":\"안녕하세요!\",\"createdAt\":\"2025-08-19T10:00:01\"}")
                    )),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{roomId}/message")
    public ResponseEntity<ChatDTO> chatCreated(
            @Parameter(name="roomId", in=ParameterIn.PATH, required=true,
                    description="채팅방 ID", example="1") @PathVariable Long roomId,
            @RequestBody ChatDTO chatRequest,
            Authentication authentication
    ) {
        String email = chatService.extractEmail(authentication); // 폼 + OAuth2 모두 지원
        User me = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 사용자 메시지 저장
        chatService.saveChat(roomId,
                new ChatDTO(me.getEmail(), chatRequest.getMessage(), LocalDateTime.now()), false);

        // 봇 응답
        String reply = chatService.AlanAiReply(chatRequest.getMessage());
        ChatDTO botMessage = new ChatDTO("chatbot", reply, LocalDateTime.now());
        chatService.saveChat(roomId, botMessage, true);

        return ResponseEntity.ok(botMessage);
    }

    @Operation(summary = "채팅방 대화 전체 삭제",
            description = "해당 채팅방의 모든 메시지를 삭제합니다. (권한 필요)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "채팅방 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{roomId}/messages")
    public ResponseEntity<Void> chatDelete(
            @Parameter(name="roomId", in=ParameterIn.PATH, required=true,
                    description="채팅방 ID", example="1") @PathVariable Long roomId,
            Authentication authentication) {
        String email = chatService.extractEmail(authentication);

        // 해당 유저가 삭제권한 있는지 확인
        chatService.deleteAllMessages(roomId, email);

        return ResponseEntity.noContent().build();
    }
}
