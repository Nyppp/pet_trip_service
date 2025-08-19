package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 채팅방의 모든 채팅을 오름차순으로 조회
    List<Chat> findAllByChatRoomIdOrderByCreatedAt(@Param("chatRoomId")Long chatRoomId);

    // 채팅방의 최신 메세지를 가져오기
    @Query("SELECT c " +
            "FROM Chat c " +
            "WHERE c.chatRoom.id = :roomId " +
            "ORDER By c.id DESC")
    Page<Chat> findLatest(@Param("roomId") Long roomId, Pageable pageable);

    // 채팅방의 메세지중 커서보다 더 예전 메세지를 가져오기
    @Query("SELECT c " +
            "FROM Chat c " +
            "WHERE c.chatRoom.id = :roomId " +
            "AND c.id < :cursor ORDER BY c.id DESC")
    Page<Chat> findOlder(@Param("roomId") Long roomId, @Param("cursor") Long cursor, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chat c WHERE c.chatRoom.id = :roomId")
    void deleteByChatRoomId(@Param("roomId") Long roomId);
}
