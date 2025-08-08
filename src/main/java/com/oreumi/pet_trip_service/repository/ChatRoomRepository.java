package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 특정 사용자가 생성한 채팅방이 있는지 조회
    // 중복 채팅방 생성 방지
    Optional<ChatRoom> findByUserId(Long userId);

    Optional<ChatRoom> findById(Long roomId);
}
