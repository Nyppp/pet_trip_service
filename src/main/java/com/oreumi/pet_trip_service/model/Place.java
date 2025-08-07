package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_code", length = 30)
    private String categoryCode;

    @Column(name = "category_name", length = 30)
    private String categoryName;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "rating", nullable = false)
    private Double rating = 0.0; // 기본값

    @Column(name = "liked", nullable = false)
    private Integer liked = 0; // 기본값

    @Column(name = "homepage_url", columnDefinition = "TEXT")
    private String homepageUrl;
}