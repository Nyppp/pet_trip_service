package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.UserSignupDTO;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        
        try {
            // 회원가입 처리 (유효성 검사는 Service 계층에서 수행)
            userService.signUp(userSignupDto);
            
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:/login";
            
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("userSignupDto", userSignupDto); // 실패 시 입력 데이터 유지
            return "user/signup";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "회원가입 중 오류가 발생했습니다. 다시 시도해 주세요.");
            model.addAttribute("userSignupDto", userSignupDto); 
            return "user/signup";
        }
    }
    


    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        // Service 계층에서 에러 메시지 처리
        if (error != null) {
            String errorMessage = userService.processLoginError(error);
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
            }
        }
        
        // Service 계층에서 로그아웃 성공 메시지 처리
        if (logout != null) {
            String successMessage = userService.processLogoutSuccess();
            model.addAttribute("successMessage", successMessage);
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
    
    /**
     * 마이페이지 표시
     */
    @GetMapping("/mypage")
    public String mypage(Model model, Authentication authentication) {
        // 현재 로그인한 사용자 정보를 모델에 추가
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
            CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();
            model.addAttribute("user", user);
        }
        return "user/mypage";
    }
    
    /**
     * 사용자 정보 업데이트
     */
    @PostMapping("/mypage/update")
    @ResponseBody
    public String updateUserInfo(@RequestParam String nickname,
                                Authentication authentication) {
        try {
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal) {
                CustomUserPrincipal userPrincipal = (CustomUserPrincipal) authentication.getPrincipal();
                User user = userPrincipal.getUser();
                
                // Service 계층에서 업데이트 처리
                userService.updateUserInfo(user.getId(), nickname);
                
                return "success";
            } else {
                return "error: 로그인 정보를 찾을 수 없습니다.";
            }
        } catch (IllegalArgumentException e) {
            return "error: " + e.getMessage();
        } catch (Exception e) {
            return "error: 사용자 정보 업데이트 중 오류가 발생했습니다.";
        }
    }
}