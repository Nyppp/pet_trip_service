package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.model.ChatRoom;


public interface ChatService {
    String getChatbotReply(String userMessage);

    String AlanAiReply(String userMessage);

    void saveChat(Long roomId, ChatDTO request, boolean isBot);

    ChatRoom createChatRoomForUser(Long userId);

}
