package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 이메일로 사용자 찾기
    Optional<User> findByEmail(String email);
    
    // 이메일, 닉네임 존재 여부 확인
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    
}
