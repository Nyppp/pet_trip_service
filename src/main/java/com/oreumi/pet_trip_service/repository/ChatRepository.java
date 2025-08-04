package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 채팅방의 모든 채팅을 오름차순으로 조회
    List<Chat> findAllByChatRoomIdOrderByCreatedAt(@Param("chatRoomId")Long chatRoomId);
}
