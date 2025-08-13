package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    //중복 체크(이메일, 닉네임)
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    // 업데이트 시 중복 체크 (자신 제외)
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByNicknameAndIdNot(String nickname, Long id);

    Optional<User> findById(Long userId);
}
