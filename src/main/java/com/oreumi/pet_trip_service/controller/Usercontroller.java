package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.UserSignupDTO;
import com.oreumi.pet_trip_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class Usercontroller {
    
    private final UserService userService;
    
    /**
     * 회원가입 페이지 표시
     */
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("userSignupDto", new UserSignupDTO());
        return "user/signup";
    }
    
    /**
     * 회원가입 처리
     */
    @PostMapping("/signup")
    public String signup(@ModelAttribute UserSignupDTO userSignupDto,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        // 유효성 검사 수행
        String validationError = validateUserSignup(userSignupDto);
        if (validationError != null) {
            model.addAttribute("errorMessage", validationError);
            return "user/signup";
        }
        
        try {
            // 회원가입 처리
            userService.signUp(userSignupDto);
            
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/signup";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다. 다시 시도해 주세요.");
            return "user/signup";
        }
    }
    
    /**
     * 회원가입 데이터 유효성 검사
     */
    private String validateUserSignup(UserSignupDTO userSignupDto) {
        // 이메일 검증
        if (userSignupDto.getEmail() == null || userSignupDto.getEmail().trim().isEmpty()) {
            return "이메일은 필수입니다.";
        }
        
        if (userSignupDto.getEmail().length() > 30) {
            return "이메일은 30자 이하여야 합니다.";
        }
        
        // 이메일 형식 검증
        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(emailPattern, userSignupDto.getEmail())) {
            return "올바른 이메일 형식이 아닙니다.";
        }
        
        // 비밀번호 검증
        if (userSignupDto.getPassword() == null || userSignupDto.getPassword().trim().isEmpty()) {
            return "비밀번호는 필수입니다.";
        }
        
        String password = userSignupDto.getPassword();
        
        // 비밀번호 길이 검증 (8-20자)
        if (password.length() < 8 || password.length() > 20) {
            return "비밀번호는 8-20자여야 합니다.";
        }
        
        // 영문, 숫자, 특수문자 각각 1개 이상 포함 검증
        if (!password.matches(".*[a-zA-Z].*")) {
            return "비밀번호는 영문자를 1개 이상 포함해야 합니다.";
        }
        
        if (!password.matches(".*[0-9].*")) {
            return "비밀번호는 숫자를 1개 이상 포함해야 합니다.";
        }
        
        if (!password.matches(".*[!@#$%^&*()\\[\\]{}|;:,.<>?].*")) {
            return "비밀번호는 특수문자를 1개 이상 포함해야 합니다.";
        }
        

        
        // 비밀번호 확인 검증
        if (userSignupDto.getPasswordConfirm() == null || userSignupDto.getPasswordConfirm().trim().isEmpty()) {
            return "비밀번호 확인은 필수입니다.";
        }
        
        // 비밀번호 일치 검증
        if (!userSignupDto.getPassword().equals(userSignupDto.getPasswordConfirm())) {
            return "비밀번호와 비밀번호 확인이 일치하지 않습니다.";
        }
        
        // 닉네임 검증
        if (userSignupDto.getNickname() == null || userSignupDto.getNickname().trim().isEmpty()) {
            return "닉네임은 필수입니다.";
        }
        
        if (userSignupDto.getNickname().length() < 2 || userSignupDto.getNickname().length() > 20) {
            return "닉네임은 2-20자여야 합니다.";
        }
        
        // 닉네임 특수문자 제한 (한글, 영문, 숫자만 허용)
        String nicknamePattern = "^[a-zA-Z0-9가-힣]+$";
        if (!Pattern.matches(nicknamePattern, userSignupDto.getNickname())) {
            return "닉네임에는 한글, 영문, 숫자만 입력 가능합니다.";
        }
        
        return null; // 유효성 검사 통과
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        if (error != null) {
            if ("true".equals(error)) {
                model.addAttribute("errorMessage", "이메일 또는 비밀번호가 올바르지 않습니다.");
            } else if ("oauth2".equals(error)) {
                model.addAttribute("errorMessage", "소셜 로그인 중 오류가 발생했습니다.");
            }
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "성공적으로 로그아웃되었습니다.");
        }
        
        return "user/login";
    }
    
    /**
     * 구글 로그인 시작
     */
    @GetMapping("/oauth2/google")
    public String googleLogin() {
        return "redirect:/oauth2/authorization/google";
    }
}