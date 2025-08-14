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
}
