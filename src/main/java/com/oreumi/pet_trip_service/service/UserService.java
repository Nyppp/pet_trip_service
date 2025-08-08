package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.UserSignupDto;
import com.oreumi.pet_trip_service.model.User;

public interface UserService {
    
    /**
     * 회원가입 처리
     * @param signupDto 회원가입 정보
     * @return 생성된 사용자 엔티티
     * @throws IllegalArgumentException 이메일 또는 닉네임 중복 시
     */
    User signUp(UserSignupDto signupDto);
    
    /**
     * 이메일 중복 체크
     * @param email 확인할 이메일
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    boolean isEmailExists(String email);
    
    /**
     * 닉네임 중복 체크
     * @param nickname 확인할 닉네임
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    boolean isNicknameExists(String nickname);
}