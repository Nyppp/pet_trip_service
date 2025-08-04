package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 채팅방의 모든 채팅을 조회
    List<Chat> findByChatRoomId(Long chatRoomId);
}
