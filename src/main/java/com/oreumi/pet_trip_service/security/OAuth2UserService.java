package com.oreumi.pet_trip_service.security;

import com.oreumi.pet_trip_service.model.Enum.AuthProvider;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.oreumi.pet_trip_service.model.Enum.UserStatus;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 구글 로그인 처리
        if ("google".equals(userRequest.getClientRegistration().getRegistrationId())) {
            User user = processGoogleUser(oAuth2User);
            return CustomUserPrincipal.create(user, oAuth2User.getAttributes());
        }
        
        // 다른 OAuth2 제공자의 경우 기본 처리
        return oAuth2User;
    }

    /**
     * 구글 사용자 정보 처리 및 저장
     */
    @Transactional
    private User processGoogleUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");

        // 기존 사용자 확인 또는 새 사용자 생성
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setNickname(createUniqueNickname(name));
                    newUser.setProfileImg(picture);
                    newUser.setProvider(AuthProvider.GOOGLE);
                    newUser.setProviderId(providerId);
                    newUser.setPassword(generateSecureRandomPassword()); // OAuth 사용자는 비밀번호 없음
                    newUser.setStatus(UserStatus.ACTIVE);
                    return userRepository.save(newUser);
                });
    }
    
    /**
     * Google 이름 + timestamp로 고유한 닉네임 생성
     */
    private String createUniqueNickname(String googleName) {
        // Google 이름에서 공백 제거 및 특수문자 처리 (한글, 영문, 숫자만 허용)
        String baseName = googleName.replaceAll("[^a-zA-Z0-9가-힣]", "");
        
        // baseName이 비어있으면 기본값 사용
        if (baseName.isEmpty()) {
            baseName = "User";
        }
        
        // timestamp 생성 (yyyyMMddHHmmss 형식)
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        String nickname = baseName + timestamp;
        
        // 닉네임 길이가 20자를 초과하면 조정
        if (nickname.length() > 20) {
            // baseName을 줄여서 총 길이가 20자 이하가 되도록 조정
            int maxBaseLength = 20 - timestamp.length();
            if (maxBaseLength > 0) {
                baseName = baseName.substring(0, Math.min(baseName.length(), maxBaseLength));
                nickname = baseName + timestamp;
            } else {
                // baseName이 너무 짧으면 timestamp만 사용
                nickname = "User" + timestamp;
            }
        }
        
        // 중복 검사 및 처리
        return ensureUniqueNickname(nickname);
    }

    //닉네임 중복 검사
    private String ensureUniqueNickname(String baseNickname) {
        String nickname = baseNickname;
        int counter = 1;
        
        // 중복 검사
        while (userRepository.existsByNickname(nickname)) {
            nickname = baseNickname + counter;
            counter++;
            
            // 무한 루프 방지 (최대 1000번 시도)
            if (counter > 1000) {
                nickname = "User" + System.nanoTime();
                break;
            }
        }
        
        return nickname;
    }

    private String generateSecureRandomPassword() {
        // 안전한 랜덤 비밀번호 생성 (UUID + 타임스탬프)
        return UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
    }
}
