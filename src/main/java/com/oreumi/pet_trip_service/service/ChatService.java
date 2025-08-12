package com.oreumi.pet_trip_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.model.Chat;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.ChatRepository;
import com.oreumi.pet_trip_service.repository.ChatRoomRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class ChatService {

    @Value("${alanApi.ai.client-id}")
    private String clientId;;

    @Value("${alanApi.ai.url}")
    private String alanAiUrl;;

    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    public String AlanAiReply(String userMessage) {
        try {
            String url = alanAiUrl +
                    URLEncoder.encode(userMessage, StandardCharsets.UTF_8) +
                    "&client_id=" + clientId;
            RestTemplate restTemplate = new RestTemplate();
            String aiJson = restTemplate.getForObject(url, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(aiJson);

            String content = root.get("content").asText();
            return content;
        } catch (Exception e) {
            return "AI 응답 오류: " + e.getMessage();
        }
    }


    public void saveChat(Long roomId, ChatDTO request, boolean isBot) {
        Chat chat = new Chat();

        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        chat.setChatRoom(room);
        chat.setChatBot(isBot);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setContent(request.getMessage());

        chatRepository.save(chat);
    }

    @Transactional
    public ChatRoom createChatRoomForUser(Long userId) {
        return getOrCreateChatRoomForUser(userId);
    }

    @Transactional
    public ChatRoom getOrCreateChatRoomForUser(Long userId) {
        return chatRoomRepository.findByUserId(userId)
                .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저"));
                                    ChatRoom room = new ChatRoom();
                                    room.setUser(user);
                                    return chatRoomRepository.save(room);
                                });
    }

    public String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        // 폼 로그인
        if (principal instanceof CustomUserPrincipal cup) {
            return cup.getUsername();
        }

        // OAuth2 로그인
        if (principal instanceof org.springframework.security.oauth2.core.user.DefaultOAuth2User ou) {
            Object emailAttr = ou.getAttributes().get("email");
            if (emailAttr instanceof String) return (String) emailAttr;
        }

        throw new IllegalStateException("이메일을 추출할 수 없습니다.");
    }



}
