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

    public ReviewDTO create(Long userId, Long pathPlaceId, ReviewDTO dto) {
        // 입력값 검증 (트랜잭션 밖에서)
        if (dto.getRating() == null || Math.round(dto.getRating() * 2) != dto.getRating() * 2) {
            throw new IllegalArgumentException("별점은 0.5 단위로 입력해 주세요.");
        }
        if (dto.getPetInfos() == null || dto.getPetInfos().isEmpty()) {
            throw new IllegalArgumentException("반려동물 정보를 최소 1개 입력해 주세요.");
        }

        // 사용자 및 장소 조회 (트랜잭션 밖에서)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Long placeId = (dto.getPlaceId() != null) ? dto.getPlaceId() : pathPlaceId;
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));

        // Base64 이미지들을 S3에 업로드 (트랜잭션 밖에서 처리)
        List<String> uploadedImageUrls = new ArrayList<>();
        try {
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                uploadedImageUrls = s3Service.uploadReviewBase64Images(dto.getImages());
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }

        try {
            // DB 트랜잭션 실행
            return createReviewTransaction(user, place, dto, uploadedImageUrls);
        } catch (Exception e) {
            // 실패 시 업로드된 이미지들 삭제 (보상 트랜잭션)
            rollbackUploadedImages(uploadedImageUrls);
            throw e;
        }
    }

    /**
     * 리뷰 생성의 DB 트랜잭션 부분 (S3 업로드 후 실행)
     */
    @Transactional
    private ReviewDTO createReviewTransaction(User user, Place place, ReviewDTO dto, List<String> uploadedImageUrls) {
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

        // 업로드된 이미지 URL들을 ReviewImg 엔티티로 저장
        for (String imageUrl : uploadedImageUrls) {
            ReviewImg reviewImg = new ReviewImg();
            reviewImg.setReview(saved);
            reviewImg.setImgURL(imageUrl);
            reviewImgRepository.save(reviewImg);  // 개별 저장
        }

        // 장소 평점 재계산
        recomputeAndUpdatePlaceRating(place.getId());

        // DTO 변환 및 반환 - 저장된 이미지들 조회
        List<ReviewImg> savedImages = reviewImgRepository.findAllByReviewIdOrderById(saved.getId());
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

    /**
     * Review 엔티티를 ReviewDTO로 변환하는 공통 메서드
     */
    private ReviewDTO convertToDTO(Review review) {
        // 이미지를 ID 순서대로 조회 (업로드 순서와 일치)
        List<ReviewImg> orderedImages = reviewImgRepository.findAllByReviewIdOrderById(review.getId());
        List<String> imgs = orderedImages.stream().map(ReviewImg::getImgURL).toList();
        
        List<PetInfoDTO> pets = review.getPetInfos().stream()
                .map(pi -> new PetInfoDTO(pi.getPetType(), pi.getBreed(), pi.getWeightKg()))
                .toList();

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser() != null ? review.getUser().getId() : null);
        dto.setPlaceId(review.getPlace() != null ? review.getPlace().getId() : null);
        dto.setPlaceName(review.getPlace() != null ? review.getPlace().getName() : null);
        dto.setRating(review.getRating());
        dto.setContent(review.getContent());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setNickname(review.getUser() != null ? review.getUser().getNickname() : null);
        dto.setUserProfileImageUrl(review.getUser() != null ? review.getUser().getProfileImg() : null);
        dto.setImages(imgs);
        dto.setPetInfos(pets);
        
        return dto;
    }

    // 버튼 노출 제어용
    @Transactional
    public boolean hasReview(Long userId, Long placeId) {
        return reviewRepository.existsByUserIdAndPlaceId(userId, placeId);
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
                .map(this::convertToDTO)
                .toList();
    }

    // 리뷰 수정
    public ReviewDTO updateReview(Long reviewId, Long userId, ReviewDTO dto) {
        // 입력값 검증
        if (dto.getRating() == null || Math.round(dto.getRating() * 2) != dto.getRating() * 2) {
            throw new IllegalArgumentException("별점은 0.5 단위로 입력해 주세요.");
        }
        if (dto.getPetInfos() == null || dto.getPetInfos().isEmpty()) {
            throw new IllegalArgumentException("반려동물 정보를 최소 1개 입력해 주세요.");
        }

        // 리뷰 존재 여부 및 권한 확인 (트랜잭션 밖에서 미리 검증)
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 기존 이미지 URL 수집 (트랜잭션 밖에서)
        List<ReviewImg> existingImages = reviewImgRepository.findAllByReviewIdOrderById(reviewId);
        List<String> oldImageUrls = new ArrayList<>();
        for (ReviewImg img : existingImages) {
            oldImageUrls.add(img.getImgURL());
        }

        // 이미지 개수 제한 검증
        List<String> imagesToKeep = dto.getExistingImages() != null ? dto.getExistingImages() : new ArrayList<>();
        int newImageCount = dto.getImages() != null ? dto.getImages().size() : 0;
        int totalImageCount = imagesToKeep.size() + newImageCount;
        
        if (totalImageCount > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 업로드할 수 있습니다. (기존: " + imagesToKeep.size() + "장, 새로 추가: " + newImageCount + "장)");
        }

        // 새로운 이미지들을 S3에 업로드 (트랜잭션 밖에서 처리)
        List<String> uploadedImageUrls = new ArrayList<>();
        try {
            if (dto.getImages() != null && !dto.getImages().isEmpty()) {
                uploadedImageUrls = s3Service.uploadReviewBase64Images(dto.getImages());
            }
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다: " + e.getMessage(), e);
        }

        // 삭제할 이미지들 계산 (트랜잭션 밖에서)
        List<String> imagesToDelete = new ArrayList<>(oldImageUrls);
        imagesToDelete.removeAll(imagesToKeep);

        try {
            // DB 트랜잭션 실행
            ReviewDTO result = updateReviewTransaction(review, dto, uploadedImageUrls, imagesToKeep);
            
            // 트랜잭션 성공 후 S3에서 삭제할 이미지들 처리 (트랜잭션 밖에서)
            deleteS3Images(imagesToDelete);
            
            return result;
        } catch (Exception e) {
            // 실패 시 새로 업로드된 이미지들 삭제 (보상 트랜잭션)
            rollbackUploadedImages(uploadedImageUrls);
            throw e;
        }
    }

    /**
     * S3에서 이미지들을 삭제하는 메서드 (트랜잭션 밖에서 실행)
     */
    private void deleteS3Images(List<String> imageUrls) {
        for (String imageUrl : imageUrls) {
            try {
                String s3Key = s3Service.extractS3KeyFromUrl(imageUrl);
                if (s3Key != null) {
                    s3Service.deleteFile(s3Key);
                }
            } catch (Exception e) {
                // S3 삭제 실패는 로그만 남기고 계속 진행
                System.err.println("S3 이미지 삭제 실패: " + imageUrl + ", 오류: " + e.getMessage());
            }
        }
    }

    /**
     * 리뷰 수정의 DB 트랜잭션 부분 (S3 업로드 후 실행, S3 삭제는 트랜잭션 밖에서 처리)
     */
    @Transactional
    private ReviewDTO updateReviewTransaction(Review review, ReviewDTO dto, List<String> uploadedImageUrls, List<String> imagesToKeep) {
        // 리뷰 기본 정보 수정
        review.setContent(dto.getContent());
        review.setRating(dto.getRating());

        // 기존 반려동물 정보 삭제하고 새로 추가
        review.getPetInfos().clear();
        for (PetInfoDTO p : dto.getPetInfos()) {
            ReviewPetInfo petInfo = new ReviewPetInfo();
            petInfo.setReview(review);
            petInfo.setPetType(p.getType());
            petInfo.setBreed(p.getBreed());
            petInfo.setWeightKg(p.getWeightKg());
            review.getPetInfos().add(petInfo);
        }

        // 이미지 처리: 기존 이미지 유지 + 새 이미지 추가 (DB 작업만)
        // 삭제할 이미지들의 DB 레코드만 삭제 (S3 삭제는 트랜잭션 밖에서)
        List<ReviewImg> allExistingImages = reviewImgRepository.findAllByReviewIdOrderById(review.getId());
        for (ReviewImg existingImg : allExistingImages) {
            if (!imagesToKeep.contains(existingImg.getImgURL())) {
                reviewImgRepository.delete(existingImg);
            }
        }
        
        // 새로 업로드된 이미지들 추가
        for (String imageUrl : uploadedImageUrls) {
            ReviewImg reviewImg = new ReviewImg();
            reviewImg.setReview(review);
            reviewImg.setImgURL(imageUrl);
            reviewImgRepository.save(reviewImg);
        }

        // 리뷰 저장
        Review savedReview = reviewRepository.save(review);

        // 장소 평점 재계산
        recomputeAndUpdatePlaceRating(review.getPlace().getId());

        // DTO 변환 및 반환
        List<ReviewImg> finalImages = reviewImgRepository.findAllByReviewIdOrderById(savedReview.getId());
        List<String> finalImageUrls = finalImages.stream()
                .map(ReviewImg::getImgURL)
                .toList();

        List<PetInfoDTO> petInfos = savedReview.getPetInfos().stream()
                .map(pi -> new PetInfoDTO(pi.getPetType(), pi.getBreed(), pi.getWeightKg()))
                .toList();

        ReviewDTO result = new ReviewDTO();
        result.setId(savedReview.getId());
        result.setUserId(savedReview.getUser().getId());
        result.setPlaceId(savedReview.getPlace().getId());
        result.setRating(savedReview.getRating());
        result.setContent(savedReview.getContent());
        result.setCreatedAt(savedReview.getCreatedAt());
        result.setNickname(savedReview.getUser().getNickname());
        result.setUserProfileImageUrl(savedReview.getUser().getProfileImg());
        result.setImages(finalImageUrls);
        result.setPetInfos(petInfos);
        
        return result;
    }

    // 사용자별 리뷰 조회
    @Transactional
    public List<ReviewDTO> getReviewsByUser(Long userId) {
        List<Review> reviews = reviewRepository.findAllByUserIdOrderByCreatedAt(userId);
        return reviews.stream().map(this::convertToDTO).toList();
    }

    // 특정 리뷰 조회 (수정용)
    @Transactional
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        return convertToDTO(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        // 리뷰 존재 여부 및 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        
        // 작성자 본인만 삭제 가능하도록 권한 검증
        if (!review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }
        
        // S3에 업로드된 이미지들 수집 (트랜잭션 밖에서)
        List<ReviewImg> images = reviewImgRepository.findAllByReviewIdOrderById(reviewId);
        List<String> imageUrls = images.stream()
                .map(ReviewImg::getImgURL)
                .toList();
        
        // 장소 ID 저장 (평점 재계산용)
        Long placeId = review.getPlace().getId();
        
        try {
            // 데이터베이스에서 리뷰 삭제 (cascade로 연관 데이터도 자동 삭제)
            reviewRepository.delete(review);
            
            // 장소 평점 재계산
            recomputeAndUpdatePlaceRating(placeId);
            
            // 트랜잭션 성공 후 S3에서 이미지들 삭제 (트랜잭션 밖에서)
            deleteS3Images(imageUrls);
            
        } catch (Exception e) {
            // DB 삭제 실패 시 S3 삭제는 하지 않음
            throw e;
        }
    }
}
