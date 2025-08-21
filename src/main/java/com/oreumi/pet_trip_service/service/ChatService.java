package com.oreumi.pet_trip_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oreumi.pet_trip_service.DTO.ChatDTO;
import com.oreumi.pet_trip_service.DTO.MessagePageDTO;
import com.oreumi.pet_trip_service.model.Chat;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.ChatRepository;
import com.oreumi.pet_trip_service.repository.ChatRoomRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;


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
        chat.setSenderEmail(request.getSender());

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

    public MessagePageDTO getMessages(Long roomId, @Nullable Long cursor, int size) {
        Page<Chat> page = (cursor == null)
                ? chatRepository.findLatest(roomId, PageRequest.of(0, size))
                : chatRepository.findOlder(roomId, cursor, PageRequest.of(0, size));

        List<ChatDTO> items = page.getContent().stream()
                .sorted(Comparator.comparing(Chat::getId))
                .map(c-> new ChatDTO(
                        c.isChatBot() ? "chatbot" : c.getSenderEmail(),
                        c.getContent(),
                        c.getCreatedAt()))
                .toList();

        Long nextCursor = page.getContent().stream()
                .map(Chat::getId)
                .min(Long::compareTo)
                .orElse(null);

        return new MessagePageDTO(items, nextCursor, page.hasNext());
    }

    @Transactional
    public void deleteAllMessages(Long roomId, String email) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));

        // (선택) 권한 체크: 방 생성자이거나 관리자만 삭제 가능
        if (!room.getUser().getEmail().equals(email)) {
            throw new SecurityException("채팅방 대화 삭제 권한이 없습니다.");
        }

        chatRepository.deleteByChatRoomId(roomId);
    }
}
