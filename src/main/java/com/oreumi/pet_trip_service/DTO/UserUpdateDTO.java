package com.oreumi.pet_trip_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {
    private String nickname;
    private MultipartFile imageFile;
    private String profileImg; // 기존 프로필 이미지 URL (필요시 사용)
}
