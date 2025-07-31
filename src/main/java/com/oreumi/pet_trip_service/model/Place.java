package com.oreumi.pet_trip_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "place")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")  // 생략 가능하지만 명시함
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category_code", nullable = false)
    private Integer categoryCode;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "phone")
    private String phone;

    @Column(name = "rating")
    private Double rating = 0.0;

    @Column(name = "liked")
    private Integer liked = 0;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImg> images = new ArrayList<>();
}
