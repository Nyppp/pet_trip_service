package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "place_img")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceImg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "url", columnDefinition = "TEXT")
    private String url;

    @Column(name = "main_img")
    private Boolean mainImg = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;
}
