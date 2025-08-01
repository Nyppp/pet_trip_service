package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 리뷰 엔티티에 대한 모델 클래스
 * <h2>엔티티 구성 요소</h2>
 * <ul>
 *     <li>고유 ID</li>
 *     <li>유저 정보</li>
 *     <li>게시물 정보</li>
 *     <li>리뷰 내용(글)</li>
 *     <li>별점</li>
 *     <li>리뷰 이미지 리스트</li>
 *     <li>생성 시각</li>
 * </ul>
 */
@Table(name = "review")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name="place_id", nullable = false)
    private Place place;

    @Column(name="content", nullable = true)
    private String content;

    @Column(name = "rating", nullable = false)
    private Double rating;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewImg> images = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
