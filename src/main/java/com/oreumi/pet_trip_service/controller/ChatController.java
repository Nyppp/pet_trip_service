package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.DTO.MessagePageDTO;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.UserRepository;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatController {


    private final ChatService chatService;
    private final UserRepository userRepository;

    // /chatrooms/test-error
    @GetMapping("/test-error")
    public String testError() {
        throw new IllegalArgumentException("테스트 에러 발생!");
    }

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

    // 채팅방 조회
    @GetMapping("/{roomId}")
    public String chatRoomFind(@PathVariable Long roomId) {
        return "";
    }

    // 채팅방 생성
    @PostMapping
    public ResponseEntity<Long> chatRoomCreated(@RequestBody ChatDTO chatRequest) {
        Long userId = Long.parseLong(chatRequest.getSender());
        ChatRoom room = chatService.createChatRoomForUser(userId);
        return ResponseEntity.ok(room.getId());
    }

    // 채팅 조회
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<MessagePageDTO> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "30") int size,
            Authentication authentication) {

        MessagePageDTO dto = chatService.getMessages(roomId, cursor, size);
        return ResponseEntity.ok(dto);
    }

    // 채팅 생성
    @PostMapping("/{roomId}/message")
    public ResponseEntity<ChatDTO> chatCreated(
            @PathVariable Long roomId,
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

    // 채팅 삭제
    @DeleteMapping("/{roomId}/messages")
    public ResponseEntity<Void> chatDelete(
            @PathVariable Long roomId,
            Authentication authentication) {
        String email = chatService.extractEmail(authentication);

        // 해당 유저가 삭제권한 있는지 확인
        chatService.deleteAllMessages(roomId, email);

        return ResponseEntity.noContent().build();
    }
}
