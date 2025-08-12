package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.UserSignupDTO;
import com.oreumi.pet_trip_service.DTO.UserUpdateDTO;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.model.Enum.AuthProvider;
import com.oreumi.pet_trip_service.model.Enum.UserStatus;
import com.oreumi.pet_trip_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final TransactionTemplate transactionTemplate;
    
    @Transactional
    public User signUp(UserSignupDTO signupDto) {
        // 유효성 검사 수행
        validateUserSignup(signupDto);
        
        // 이메일 중복 체크
        if (userRepository.existsByEmail(signupDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 닉네임 중복 체크
        if (userRepository.existsByNickname(signupDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 사용자 엔티티 생성
        User user = new User();
        user.setEmail(signupDto.getEmail());
        user.setPassword(passwordEncoder.encode(signupDto.getPassword()));
        user.setNickname(signupDto.getNickname());
        user.setStatus(UserStatus.ACTIVE);
        user.setProvider(AuthProvider.LOCAL);
        
        return userRepository.save(user);
    }
    
    /**
     * 회원가입 데이터 유효성 검사
     */
    private void validateUserSignup(UserSignupDTO userSignupDto) {
        // 이메일 검증
        if (userSignupDto.getEmail() == null || userSignupDto.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        
        if (userSignupDto.getEmail().length() > 30) {
            throw new IllegalArgumentException("이메일은 30자 이하여야 합니다.");
        }
        
        // 이메일 형식 검증
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!userSignupDto.getEmail().matches(emailPattern)) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        
        // 비밀번호 검증
        if (userSignupDto.getPassword() == null || userSignupDto.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        
        String password = userSignupDto.getPassword();
        
        // 비밀번호 길이 검증 (8-20자)
        if (password.length() < 8 || password.length() > 20) {
            throw new IllegalArgumentException("비밀번호는 8-20자여야 합니다.");
        }
        
        // 영문, 숫자, 특수문자 각각 1개 이상 포함 검증
        if (!password.matches(".*[a-zA-Z].*")) {
            throw new IllegalArgumentException("비밀번호는 영문자를 1개 이상 포함해야 합니다.");
        }
        
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("비밀번호는 숫자를 1개 이상 포함해야 합니다.");
        }
        
        if (!password.matches(".*[!@#$%^&*()\\[\\]{}|;:,.<>?].*")) {
            throw new IllegalArgumentException("비밀번호는 특수문자를 1개 이상 포함해야 합니다.");
        }
        
        // 비밀번호 확인 검증
        if (userSignupDto.getPasswordConfirm() == null || userSignupDto.getPasswordConfirm().trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호 확인은 필수입니다.");
        }
        
        // 비밀번호 일치 검증
        if (!userSignupDto.getPassword().equals(userSignupDto.getPasswordConfirm())) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }
        
        // 닉네임 검증
        if (userSignupDto.getNickname() == null || userSignupDto.getNickname().trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        
        if (userSignupDto.getNickname().length() < 2 || userSignupDto.getNickname().length() > 20) {
            throw new IllegalArgumentException("닉네임은 2-20자여야 합니다.");
        }
        
        // 닉네임 특수문자 제한 (한글, 영문, 숫자만 허용)
        String nicknamePattern = "^[a-zA-Z0-9가-힣]+$";
        if (!userSignupDto.getNickname().matches(nicknamePattern)) {
            throw new IllegalArgumentException("닉네임에는 한글, 영문, 숫자만 입력 가능합니다.");
        }
    }

       /**
     * 로그인 에러 메시지 처리
     */
    public String processLoginError(String errorType) {
        if (errorType == null) {
            return null;
        }
        
        return switch (errorType) {
            case "true" -> "이메일 또는 비밀번호가 올바르지 않습니다.";
            case "oauth2" -> "소셜 로그인 중 오류가 발생했습니다.";
            default -> "로그인 중 오류가 발생했습니다.";
        };
    }
    
    /**
     * 로그아웃 성공 메시지 처리
     */
    public String processLogoutSuccess() {
        return "성공적으로 로그아웃되었습니다.";
    }

    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
    
    /**
     * 사용자 ID로 최신 정보 조회
     */
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    
    /**
     * 사용자 정보 업데이트 (마이페이지용)
     */
    @Transactional
    public User updateUserInfo(Long userId, String nickname) {
        // 닉네임 유효성 검사
        validateNickname(nickname);
        
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 닉네임 중복 체크 (자신의 닉네임은 제외)
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        // 사용자 정보 업데이트
        user.setNickname(nickname);
        
        return userRepository.save(user);
    }
    
    /**
     * 닉네임 유효성 검사
     */
    private void validateNickname(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수입니다.");
        }
        
        if (nickname.length() < 2 || nickname.length() > 20) {
            throw new IllegalArgumentException("닉네임은 2-20자여야 합니다.");
        }
        
        // 닉네임 특수문자 제한 (한글, 영문, 숫자만 허용)
        String nicknamePattern = "^[a-zA-Z0-9가-힣]+$";
        if (!nickname.matches(nicknamePattern)) {
            throw new IllegalArgumentException("닉네임에는 한글, 영문, 숫자만 입력 가능합니다.");
        }
    }
    
    /**
     * 기존 updateUser 메서드 (호환성을 위해 유지)
     */
    @Transactional
    public User updateUser(User user) {
        // 이메일 중복 체크 (자신의 이메일은 제외)
        if (userRepository.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 닉네임 중복 체크 (자신의 닉네임은 제외)
        if (userRepository.existsByNicknameAndIdNot(user.getNickname(), user.getId())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        
        return userRepository.save(user);
    }

    /**
     * 프로필 이미지 업데이트 (보상 트랜잭션)
     */
    public User updateProfileImage(Long userId, MultipartFile imageFile) throws IOException {
        // 이미지 파일 유효성 검사
        validateImageFile(imageFile);
        
        // 1. 이미지 업로드 (트랜잭션 밖)
        String uploadedImageUrl = s3Service.uploadProfileImage(imageFile);
        
        try {
            // 2. DB 업데이트 (프로그래매틱 트랜잭션)
            return transactionTemplate.execute(status -> 
                updateProfileImageInTransaction(userId, uploadedImageUrl)
            );
            
        } catch (Exception e) {
            // 3. 실패 시 S3 파일 삭제 (보상 트랜잭션)
            rollbackUploadedImage(uploadedImageUrl);
            throw new RuntimeException("프로필 이미지 업데이트 실패", e);
        }
    }
    
    /**
     * 트랜잭션 내에서 프로필 이미지 URL 업데이트
     */
    @Transactional
    public User updateProfileImageInTransaction(Long userId, String imageUrl) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 사용자 프로필 이미지 URL 업데이트
        user.setProfileImg(imageUrl);
        
        return userRepository.save(user);
    }
    
    /**
     * 이미지 파일 유효성 검사
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일을 선택해주세요.");
        }
        
        // 파일 크기 검사 (2MB 이하)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("이미지 파일 크기는 2MB 이하여야 합니다.");
        }
        
        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }
        
        // 허용된 이미지 형식 검사
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.toLowerCase();
            if (!extension.endsWith(".jpg") && !extension.endsWith(".jpeg") && 
                !extension.endsWith(".png") && !extension.endsWith(".gif")) {
                throw new IllegalArgumentException("JPG, JPEG, PNG, GIF 형식의 이미지만 업로드 가능합니다.");
            }
        }
    }
    
    /**
     * 사용자 정보 통합 업데이트 (이미지 + 닉네임)
     */
    public User updateUserInfoWithImage(Long userId, UserUpdateDTO userUpdateDTO) throws IOException {
        // 이미지 업로드 (트랜잭션 밖에서 처리)
        String uploadedImageUrl = null;
        if (userUpdateDTO.getImageFile() != null && !userUpdateDTO.getImageFile().isEmpty()) {
            validateImageFile(userUpdateDTO.getImageFile());
            
            // S3에 이미지 업로드 (트랜잭션 밖)
            uploadedImageUrl = s3Service.uploadProfileImage(userUpdateDTO.getImageFile());
        }
        
        try {
            // DB 업데이트
            final String finalImageUrl = uploadedImageUrl;
            return transactionTemplate.execute(status -> 
                updateUserInfoInTransaction(userId, userUpdateDTO, finalImageUrl)
            );
            
        } catch (Exception e) {
            // 실패 시 S3 파일 삭제 (보상 트랜잭션)
            if (uploadedImageUrl != null) {
                rollbackUploadedImage(uploadedImageUrl);
            }
            throw new RuntimeException("사용자 정보 업데이트 실패", e);
        }
    }
    
    /**
     * 트랜잭션 내에서 사용자 정보 업데이트
     */
    @Transactional
    public User updateUserInfoInTransaction(Long userId, UserUpdateDTO userUpdateDTO, String imageUrl) {
        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 이미지 URL 업데이트 (이미지가 업로드된 경우)
        if (imageUrl != null) {
            user.setProfileImg(imageUrl);
        }
        
        // 닉네임 업데이트 (닉네임이 제공된 경우)
        if (userUpdateDTO.getNickname() != null && !userUpdateDTO.getNickname().trim().isEmpty()) {
            validateNickname(userUpdateDTO.getNickname());
            
            // 닉네임 중복 체크 (자신의 닉네임은 제외)
            if (userRepository.existsByNicknameAndIdNot(userUpdateDTO.getNickname(), userId)) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            
            // 사용자 닉네임 업데이트
            user.setNickname(userUpdateDTO.getNickname());
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 업로드된 이미지 롤백 (보상 트랜잭션)
     */
    private void rollbackUploadedImage(String imageUrl) {
        if (imageUrl == null) {
            return;
        }
        
        try {
            String s3Key = s3Service.extractS3KeyFromUrl(imageUrl);
            if (s3Key != null) {
                s3Service.deleteFile(s3Key);
            }
        } catch (Exception e) {
            // 롤백 실패는 예외를 다시 던지지 않음
        }
    }
} 