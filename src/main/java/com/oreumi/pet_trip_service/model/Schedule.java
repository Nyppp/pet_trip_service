package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * 스케쥴(일정) 엔티티에 대한 모델 클래스
 * <h2>엔티티 구성 요소</h2>
 * <ul>
 *     <li>고유 ID</li>
 *     <li>유저 정보(스케쥴 제작자)</li>
 *     <li>유저 정보(참가자)</li>
 *     <li>스케쥴 아이템</li>
 *     <li>일정</li>
 *     <li>생성 시각</li>
 * </ul>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule")
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "schedule_participant",
            joinColumns = @JoinColumn(name="schedule_id"),
            inverseJoinColumns = @JoinColumn(name="user_id")
    )
    private Set<User> participants = new HashSet<>();

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ScheduleItem> scheduleItems = new HashSet<>();

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
