// src/main/java/com/oreumi/pet_trip_service/model/ReviewPetInfo.java
package com.oreumi.pet_trip_service.model;

import com.oreumi.pet_trip_service.model.Enum.PetType;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "review_petinfo")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewPetInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", nullable = false, length = 10)
    private PetType petType;

    @Column(name = "breed", length = 50)
    private String breed;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;
}
