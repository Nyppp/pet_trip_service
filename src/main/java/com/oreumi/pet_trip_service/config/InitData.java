package com.oreumi.pet_trip_service.config;

import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.model.ChatRoom;
import com.oreumi.pet_trip_service.model.Enum.AuthProvider;
import com.oreumi.pet_trip_service.model.Enum.UserStatus;
import com.oreumi.pet_trip_service.repository.UserRepository;
import com.oreumi.pet_trip_service.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InitData {
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;

    @PostConstruct
    public void init() {
        if (!userRepository.existsById(1L)) {
            User user = new User(
                    null,
                    "test1@sample.com",
                    "password123",
                    "비회원 유저",
                    UserStatus.ACTIVE,
                    AuthProvider.LOCAL,
                    null,
                    null,
                    LocalDateTime.now()
            );
            user = userRepository.save(user);

            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setUser(user);
            chatRoomRepository.save(chatRoom);
        }
    }
}
