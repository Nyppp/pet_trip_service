package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.PlaceDTO;
import com.oreumi.pet_trip_service.model.Like;
import com.oreumi.pet_trip_service.model.Place;
import com.oreumi.pet_trip_service.model.PlaceImg;
import com.oreumi.pet_trip_service.model.User;
import com.oreumi.pet_trip_service.repository.LikeRepository;
import com.oreumi.pet_trip_service.repository.PlaceImgRepository;
import com.oreumi.pet_trip_service.repository.PlaceRepository;
import com.oreumi.pet_trip_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final PlaceImgRepository placeImgRepository;

    public boolean isLiked(Long userId, Long placeId) {
        return likeRepository.findByUserIdAndPlaceId(userId, placeId).isPresent();
    }

    @Transactional
    public int like(Long userId, Long placeId) {
        // 이미 좋아요면 카운트만 반환(멱등)
        if (isLiked(userId, placeId)) {
            return placeRepository.findById(placeId)
                    .map(Place::getLiked)
                    .orElse(0);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found"));
        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));

        Like like = new Like();
        like.setUser(user);
        like.setPlace(place);
        likeRepository.save(like);

        place.setLiked(Math.max(0, place.getLiked() + 1));
        placeRepository.save(place);

        return place.getLiked();
    }

    @Transactional
    public int unlike(Long userId, Long placeId) {
        likeRepository.findByUserIdAndPlaceId(userId, placeId)
                .ifPresent(likeRepository::delete);

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("place not found"));
        place.setLiked(Math.max(0, place.getLiked() - 1));
        placeRepository.save(place);

        return place.getLiked();
    }




    //사용자가 찜한 장소 목록을 이미지 정보와 함께 PlaceDTO로 조회
    public List<PlaceDTO> getLikedPlacesWithImagesByUser(Long userId) {
        List<Like> likes = likeRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return likes.stream()
                .map(like -> convertToPlaceDTO(like.getPlace()))
                .collect(Collectors.toList());
    }

    //Place 엔티티를 PlaceDTO로 변환합니다 (이미지 포함).
    private PlaceDTO convertToPlaceDTO(Place place) {
        List<String> imageUrls = placeImgRepository.findByPlace(place).stream()
                .map(PlaceImg::getUrl)
                .collect(Collectors.toList());

        return new PlaceDTO(
                place.getId(),
                place.getName(),
                place.getDescription(),
                place.getCategoryCode(),
                place.getCategoryName(),
                place.getAddress(),
                place.getLat(),
                place.getLng(),
                place.getPhone(),
                place.getRating(),
                place.getLiked(),
                place.getHomepageUrl(),
                place.getAiReview(),
                place.getAiPet(),
                imageUrls
        );
    }
}
