package com.oreumi.pet_trip_service.controller;

import com.oreumi.pet_trip_service.DTO.ImageResponseDTO;
import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.DTO.UserSignupDTO;
import com.oreumi.pet_trip_service.DTO.UserUpdateDTO;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.security.CustomUserPrincipal;
import com.oreumi.pet_trip_service.service.LikeService;
import com.oreumi.pet_trip_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final LikeService likeService;


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
    public String mypage(Model model, @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletResponse response) {
        // 현재 로그인한 사용자 정보를 DB에서 최신으로 조회
        if (principal != null) {
            Long userId = principal.getUser().getId();

            // DB에서 최신 사용자 정보 조회
            User user = userService.findById(userId);
            model.addAttribute("user", user);
        }
        // 캐시 방지 헤더 설정
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        return "user/mypage";
    }

    /**
     * 찜한 장소 목록 페이지 표시
     */
    @GetMapping("/mypage/likes")
    public String mypageLikes(Model model, @AuthenticationPrincipal CustomUserPrincipal principal, HttpServletResponse response) {
        if (principal == null) {
            return "redirect:/login";
        }

        // 현재 로그인한 사용자의 찜한 장소 목록 조회
        Long userId = principal.getUser().getId();
        List<PlaceDTO> likedPlaces = likeService.getLikedPlacesWithImagesByUser(userId);

        model.addAttribute("likedPlaces", likedPlaces);
        model.addAttribute("user", principal.getUser());

        // 캐시 방지 헤더 설정
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        return "user/mypage_likes";
    }



    /**
     * 사용자 정보 통합 업데이트 (이미지 + 닉네임)
     */
    @PostMapping("/api/user/update")
    @ResponseBody
    public ResponseEntity<ImageResponseDTO> updateUserInfoWithImage(
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "nickname", required = false) String nickname,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            HttpServletRequest request) {

        try {
            if (principal == null) {
                return ResponseEntity.badRequest()
                        .body(ImageResponseDTO.builder()
                                .success(false)
                                .message("로그인이 필요합니다.")
                                .build());
            }

            User user = principal.getUser();

            // UserUpdateDTO 생성
            UserUpdateDTO userUpdateDTO = UserUpdateDTO.builder()
                    .imageFile(imageFile)
                    .nickname(nickname)
                    .build();

            // 이미지와 닉네임 통합 업데이트
            User updatedUser = userService.updateUserInfoWithImage(user.getId(), userUpdateDTO);

            // Authentication 세션 업데이트
            updateAuthenticationSession(updatedUser, request);

            return ResponseEntity.ok(ImageResponseDTO.builder()
                    .success(true)
                    .message("사용자 정보가 성공적으로 업데이트되었습니다.")
                    .imageUrl(updatedUser.getProfileImg())
                    .build());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ImageResponseDTO.builder()
                            .success(false)
                            .message(e.getMessage())
                            .build());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(ImageResponseDTO.builder()
                            .success(false)
                            .message("이미지 업로드 중 오류가 발생했습니다.")
                            .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ImageResponseDTO.builder()
                            .success(false)
                            .message("사용자 정보 업데이트 중 오류가 발생했습니다.")
                            .build());
        }
    }

    /**
     * Authentication 세션 업데이트
     */
    private void updateAuthenticationSession(User updatedUser, HttpServletRequest request) {
        // 새로운 CustomUserPrincipal 생성
        CustomUserPrincipal newUserPrincipal = CustomUserPrincipal.create(updatedUser);

        // 새로운 Authentication 객체 생성
        UsernamePasswordAuthenticationToken newAuth =
                new UsernamePasswordAuthenticationToken(newUserPrincipal, null, newUserPrincipal.getAuthorities());

        // SecurityContext 업데이트
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // 세션에 SecurityContext 저장
        HttpSession session = request.getSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
    }
}