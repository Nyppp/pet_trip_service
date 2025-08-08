package com.oreumi.pet_trip_service.service;

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

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 구글 로그인 처리
        if ("google".equals(userRequest.getClientRegistration().getRegistrationId())) {
            processGoogleUser(oAuth2User);
        }
        
        return oAuth2User;
    }

    /**
     * 구글 사용자 정보 처리 및 저장
     */
    @Transactional
    private void processGoogleUser(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");
        String providerId = (String) attributes.get("sub");

        // 기존 사용자 확인 또는 새 사용자 생성
        userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setNickname(name);
                    newUser.setProfileImg(picture);
                    newUser.setProvider(AuthProvider.GOOGLE);
                    newUser.setProviderId(providerId);
                    newUser.setPassword(""); // OAuth 사용자는 비밀번호 없음
                    return userRepository.save(newUser);
                });
    }
} 