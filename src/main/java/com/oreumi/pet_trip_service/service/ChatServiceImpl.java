package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.model.Chat;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.ChatRepository;
import com.oreumi.pet_trip_service.repository.ChatRoomRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    public String getChatbotReply(String userMessage) {
        if (userMessage.contains("안녕")) {
            return "안녕하세요! 무엇을 도와드릴까요?";
        }
        return "죄송합니다. 이해하지 못했어요.";
    }

    @Override
    public void saveChat(Long roomId, ChatDTO request, boolean isBot) {
        Chat chat = new Chat();

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        chat.setChatRoom(room);
        chat.setChatBot(isBot);
        chat.setCreatedAt(request.getSendAt());
        chat.setContent(request.getMessage());

        chatRepository.save(chat);
    }

    @Override
    public ChatRoom createChatRoomForUser(Long userId) {

        Optional<ChatRoom> exist = chatRoomRepository.findByUserId(userId);
        if (exist.isPresent()) {
            return exist.get();
        }

        ChatRoom chatRoom = new ChatRoom();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        chatRoom.setUser(user);

        return chatRoomRepository.save(chatRoom);
    }

}
