package com.oreumi.pet_trip_service.security;

import com.oreumi.pet_trip_service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 폼 로그인과 OAuth2 로그인을 통합 처리하는 커스텀 Principal 클래스
 * UserDetails와 OAuth2User 인터페이스를 모두 구현하여 일관된 방식으로 사용자 정보 접근 가능
 */
@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails, OAuth2User {
    
    private final User user;
    private final Map<String, Object> attributes;
    
    /**
     * 폼 로그인용 생성자
     */
    public static CustomUserPrincipal create(User user) {
        return new CustomUserPrincipal(user, null);
    }
    
    /**
     * OAuth2 로그인용 생성자
     */
    public static CustomUserPrincipal create(User user, Map<String, Object> attributes) {
        return new CustomUserPrincipal(user, attributes);
    }
    
    /**
     * User 엔티티 반환 - 컨트롤러에서 직접 사용
     */
    public User getUser() {
        return user;
    }
    
    // ===== UserDetails 인터페이스 구현 =====
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return user.getStatus().name().equals("ACTIVE");
    }
    
    // ===== OAuth2User 인터페이스 구현 =====
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    
    @Override
    public String getName() {
        // OAuth2User의 getName()은 고유 식별자를 반환해야 함
        if (attributes != null) {
            // Google의 경우 'sub' 필드가 고유 식별자
            Object sub = attributes.get("sub");
            if (sub != null) {
                return sub.toString();
            }
        }
        // fallback으로 이메일 사용
        return user.getEmail();
    }
}
