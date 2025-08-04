package com.oreumi.pet_trip_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor // 필수 생성자 생성인듯
public class ChatController {

    // 채팅방 조회
    @GetMapping("/{roomId}")
    public String chatRoomFind(@PathVariable Long roomId) {
        return "";
    }

    @PostMapping
    public String chatRoomCreated(@RequestBody Dto dto) {
        return "";
    }

    @GetMapping("/{roomId}/message")
    public String chatFind(@PathVariable Long roomId) {
        return "";
    }

    @PostMapping("/{roomId}/message")
    public String chatCreated(@RequestBody Dto dto) {
        return "";
    }

    @DeleteMapping("/messages/{messageId}")
    public String chatDelete(@PathVariable Long messageId) {
        return "";
    }

    // 채팅창은 모달창으로 만들예정
    // 채팅 모달을 띄우는 버튼은 어느페이지든 있음.
    // 위 메서드는 모두 Restful Api로 바꿀것
}
