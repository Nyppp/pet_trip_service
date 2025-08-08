package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.*;

import java.time.LocalDateTime;

/**
 * 게시물 좋아요 엔티티에 대한 모델 클래스
 * <h2>엔티티 구성 요소</h2>
 * <ul>
 *     <li>고유 ID</li>
 *     <li>유저 정보</li>
 *     <li>게시물 정보</li>
 *     <li>생성 시각</li>
 * </ul>
 */
@Table(name = "likes")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
