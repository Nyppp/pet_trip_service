package com.oreumi.pet_trip_service.controller;


import com.oreumi.pet_trip_service.DTO.ChatRequest;
import com.oreumi.pet_trip_service.DTO.ChatResponse;
import com.oreumi.pet_trip_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor // 필수 생성자 생성인듯
public class ChatController {


    private final ChatService chatService;
  
    // 채팅방 조회
    @GetMapping("/{roomId}")
    public String chatRoomFind(@PathVariable Long roomId) {
        return "";
    }

    @PostMapping
    public String chatRoomCreated(@RequestBody ChatRequest dto) {
        return "";
    }

    @GetMapping("/{roomId}/message")
    public String chatFind(@PathVariable Long roomId) {
        return "";
    }

    @PostMapping("/{roomId}/message")
    public ResponseEntity<ChatResponse> chatCreated(@PathVariable Long roomId, @RequestBody ChatRequest chatRequest) {
        System.out.println("사용자 메세지: " + chatRequest.getMessage());

        String reply = chatService.getChatbotReply(chatRequest.getMessage());
        System.out.println("챗봇 응답: " + reply);

        return ResponseEntity.ok(new ChatResponse(reply));
    }

    @DeleteMapping("/messages/{messageId}")
    public String chatDelete(@PathVariable Long messageId) {
        return "";
    }
}
