package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.UserRepository;
import com.oreumi.pet_trip_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
public class ChatController {


    private final ChatService chatService;
  
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
    @GetMapping("/{roomId}/message")
    public String chatFind(@PathVariable Long roomId) {
        return "";
    }

    // 채팅 생성
    @PostMapping("/{roomId}/message")
    public ResponseEntity<ChatDTO> chatCreated(@PathVariable Long roomId, @RequestBody ChatDTO chatRequest) {
        System.out.println("사용자 메세지: " + chatRequest.getMessage() + chatRequest.getSendAt());
        chatService.saveChat(roomId, chatRequest, false);

        String reply = chatService.AlanAiReply(chatRequest.getMessage());
        System.out.println("챗봇 응답: " + reply);

        ChatDTO botMessage = new ChatDTO("chatbot", reply, LocalDateTime.now());
        chatService.saveChat(roomId, botMessage, true);

        return ResponseEntity.ok(new ChatDTO("chatbot", reply, LocalDateTime.now()));
    }

    // 채팅 삭제
    @DeleteMapping("/messages/{messageId}")
    public String chatDelete(@PathVariable Long messageId) {
        return "";
    }

}
