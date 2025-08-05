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

/**
 * 스케쥴 아이템에 대한 모델 클래스
 * <h2>엔티티 구성 요소</h2>
 * <ul>
 *     <li>고유 ID</li>
 *     <li>스케쥴 정보(상위 엔티티)</li>
 *     <li>장소 정보</li>
 *     <li>일정 시작 시간</li>
 *     <li>일정 종료 시간</li>
 *     <li>메모</li>
 *     <li>생성 시각</li>
 * </ul>
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "schedule_item")
public class ScheduleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="schedule_id", nullable = false)
    private Schedule schedule;

//    장소 > 개발중으로 임시 주석처리
//    @ManyToOne
//    @JoinColumn(name = "place_id", nullable = false)
//    private Place place;

//    장소 대체 > 타이틀로 우선 저장하도록
    @Column(name="item_title")
    private String itemTitle;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "memo", nullable = true, length = 500)
    private String memo;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
