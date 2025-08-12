package com.oreumi.pet_trip_service.model;

import com.oreumi.pet_trip_service.model.Enum.Category;
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

    @Column(name="ai_review", columnDefinition="TEXT")
    private String aiReview;

    @Column(name="ai_pet", columnDefinition="TEXT")
    private String aiPet;

    @Transient
    public Category getCategoryEnum() {
        return Category.fromCode(this.categoryCode); // enum name() 기반
    }

    @Transient
    public Category getCat1() {
        return Category.getCat1FromCat3(getCategoryEnum());
    }

    @Transient
    public Category getCat2() {
        return Category.getCat2FromCat3(getCategoryEnum());
    }

    @Transient
    public String getCategoryName() {
        return getCategoryEnum().getDescription();
    }
}