package com.oreumi.pet_trip_service.service;

import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Override
    public String getChatbotReply(String userMessage) {
        if (userMessage.contains("안녕")) {
            return "안녕하세요! 무엇을 도와드릴까요?";
        }
        return "죄송합니다. 이해하지 못했어요.";
    }
}
