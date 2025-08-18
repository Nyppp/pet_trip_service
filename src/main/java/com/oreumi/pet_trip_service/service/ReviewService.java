package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.PetInfoDTO;
import com.oreumi.pet_trip_service.DTO.ReviewDTO;
import com.oreumi.pet_trip_service.model.*;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import com.oreumi.pet_trip_service.repository.ReviewImgRepository;
import com.oreumi.pet_trip_service.repository.ReviewRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImgRepository reviewImgRepository;
    private final UserRepository userRepository;
    private final PlaceRepository placeRepository;
    private final S3Service s3Service;

    @Transactional
    public ReviewDTO create(Long userId, Long pathPlaceId, ReviewDTO dto) {
        if (dto.getRating() == null || Math.round(dto.getRating() * 2) != dto.getRating() * 2) {
            throw new IllegalArgumentException("별점은 0.5 단위로 입력해 주세요.");
        }
        if (dto.getPetInfos() == null || dto.getPetInfos().isEmpty()) {
            throw new IllegalArgumentException("반려동물 정보를 최소 1개 입력해 주세요.");
        }

        // 1. Base64 이미지들을 S3에 업로드 (트랜잭션 밖에서 처리)
        List<String> uploadedImageUrls = new ArrayList<>();
        try {
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                uploadedImageUrls = s3Service.uploadReviewBase64Images(dto.getImages());
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }

        try {
            // 2. 사용자 및 장소 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("user not found"));
            Long placeId = (dto.getPlaceId() != null) ? dto.getPlaceId() : pathPlaceId;
            Place place = placeRepository.findById(placeId)
                    .orElseThrow(() -> new IllegalArgumentException("place not found"));

        Review review = new Review();
        review.setUser(user);
        review.setPlace(place);
        review.setContent(dto.getContent());
        review.setRating(dto.getRating());

        for (PetInfoDTO p : dto.getPetInfos()) {
            ReviewPetInfo e = new ReviewPetInfo();
            e.setReview(review);                 // 양방향 연결
            e.setPetType(p.getType());
            e.setBreed(p.getBreed());
            e.setWeightKg(p.getWeightKg());
            review.getPetInfos().add(e);
        }

                 Review saved = reviewRepository.save(review);

            // 6. 업로드된 이미지 URL들을 ReviewImg 엔티티로 저장
            for (String imageUrl : uploadedImageUrls) {
                ReviewImg reviewImg = new ReviewImg();
                reviewImg.setReview(saved);
                reviewImg.setImgURL(imageUrl);
                reviewImgRepository.save(reviewImg);  // 개별 저장
            }

            // 8. 장소 평점 재계산
            recomputeAndUpdatePlaceRating(place.getId());

            // 9. DTO 변환 및 반환 - 저장된 이미지들 조회
            List<ReviewImg> savedImages = reviewImgRepository.findAllByReviewId(saved.getId());
            List<String> finalImageUrls = savedImages.stream()
                    .map(ReviewImg::getImgURL)
                    .toList();

            List<PetInfoDTO> petInfos = saved.getPetInfos().stream()
                    .map(pi -> new PetInfoDTO(pi.getPetType(), pi.getBreed(), pi.getWeightKg()))
                    .toList();

            ReviewDTO res = new ReviewDTO();
            res.setId(saved.getId());
            res.setUserId(user.getId());
            res.setPlaceId(place.getId());
            res.setRating(saved.getRating());
            res.setContent(saved.getContent());
            res.setCreatedAt(saved.getCreatedAt());
            res.setNickname(user.getNickname());
            res.setUserProfileImageUrl(user.getProfileImg());
            res.setImages(finalImageUrls);
            res.setPetInfos(petInfos);
            return res;

        } catch (Exception e) {
            // 실패 시 업로드된 이미지들 삭제 (보상 트랜잭션)
            rollbackUploadedImages(uploadedImageUrls);
            throw e;
        }
    }
    
    /**
     * 업로드된 이미지들 롤백 (보상 트랜잭션)
     */
    private void rollbackUploadedImages(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                String s3Key = s3Service.extractS3KeyFromUrl(imageUrl);
                if (s3Key != null) {
                    s3Service.deleteFile(s3Key);
                }
            } catch (Exception e) {
                // 롤백 실패는 로그만 남기고 무시
                System.err.println("Failed to rollback image: " + imageUrl + ", error: " + e.getMessage());
            }
        }
    }

    // 버튼 노출 제어용
    @Transactional
    public boolean hasReview(Long userId, Long placeId) {
        return reviewRepository.existsByUserIdAndPlaceId(userId, placeId);
    }


    @Transactional
    public List<ReviewDTO> listByPlace(Long placeId) {
        List<Review> list = reviewRepository.findAllByPlaceIdOrderByCreatedAtDesc(placeId);

        return list.stream().map(r -> {
            List<String> imgs = r.getImages().stream().map(ReviewImg::getImgURL).toList();
            List<PetInfoDTO> pets = r.getPetInfos().stream()
                    .map(pi -> new PetInfoDTO(pi.getPetType(), pi.getBreed(), pi.getWeightKg()))
                    .toList();

            ReviewDTO d = new ReviewDTO();
            d.setId(r.getId());
            d.setUserId(r.getUser() != null ? r.getUser().getId() : null);
            d.setPlaceId(r.getPlace() != null ? r.getPlace().getId() : null);
            d.setRating(r.getRating());
            d.setContent(r.getContent());
            d.setCreatedAt(r.getCreatedAt());
            d.setNickname(r.getUser() != null ? r.getUser().getNickname() : null);
            d.setUserProfileImageUrl(r.getUser() != null ? r.getUser().getProfileImg() : null);
            d.setImages(imgs);
            d.setPetInfos(pets);
            return d;
        }).toList();
    }
    private void recomputeAndUpdatePlaceRating(Long placeId) {
        Double avg = reviewRepository.findAverageRatingByPlaceId(placeId);
        double value = (avg == null) ? 0.0 : Math.round(avg * 10.0) / 10.0;
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));
        place.setRating(value);
        placeRepository.save(place);
    }

    @Transactional
    public List<ReviewDTO> getReviewsByPlace(Long placeId) {
        return reviewRepository.findAllByPlaceIdOrderByCreatedAtDesc(placeId)
                .stream()
                .map(r -> {
                    ReviewDTO dto = new ReviewDTO();
                    dto.setId(r.getId());
                    dto.setUserId(r.getUser().getId());
                    dto.setPlaceId(r.getPlace().getId());
                    dto.setRating(r.getRating());
                    dto.setContent(r.getContent());
                    dto.setCreatedAt(r.getCreatedAt());

                    dto.setNickname(r.getUser().getNickname());
                    dto.setUserProfileImageUrl(r.getUser().getProfileImg());

                    dto.setImages(
                            r.getImages().stream()
                                    .map(ReviewImg::getImgURL)
                                    .toList()
                    );

                    dto.setPetInfos(
                            r.getPetInfos().stream()
                                    .map(pi -> new PetInfoDTO(pi.getPetType(), pi.getBreed(), pi.getWeightKg()))
                                    .toList()
                    );

                    return dto;
                })
                .toList();
    }

    /**
     * 리뷰 삭제
     * @param reviewId 삭제할 리뷰 ID
     * @param userId 요청하는 사용자 ID (권한 검증용)
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 1. 리뷰 존재 여부 및 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        // 2. 작성자 본인만 삭제 가능하도록 권한 검증
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }
        
        // 3. S3에 업로드된 이미지들 삭제
        List<ReviewImg> images = reviewImgRepository.findAllByReviewId(reviewId);
        for (ReviewImg img : images) {
            try {
                String s3Key = s3Service.extractS3KeyFromUrl(img.getImgURL());
                if (s3Key != null) {
                    s3Service.deleteFile(s3Key);
                }
            } catch (Exception e) {
                // 이미지 삭제 실패는 로그만 남기고 계속 진행
                System.err.println("이미지 삭제 실패: " + img.getImgURL() + ", 오류: " + e.getMessage());
            }
        }
        
        // 4. 장소 ID 저장 (평점 재계산용)
        Long placeId = review.getPlace().getId();
        
        // 5. 데이터베이스에서 리뷰 삭제 (cascade로 연관 데이터도 자동 삭제)
        reviewRepository.delete(review);
        
        // 6. 장소 평점 재계산
        recomputeAndUpdatePlaceRating(placeId);
    }
}
