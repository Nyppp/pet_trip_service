package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 리뷰 이미지 엔티티에 대한 모델 클래스
 * <h2>엔티티 구성 요소</h2>
 * <ul>
 *     <li>고유 ID</li>
 *     <li>리뷰 정보(review_id)</li>
 *     <li>리뷰 이미지 url</li>
 * </ul>
 */
@Table(name="review_img")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="review_id", nullable = false)
    private Review review;

    @Column(name = "img_url", nullable = false)
    private String imgURL;
}
