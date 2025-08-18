package com.oreumi.pet_trip_service.DTO;

import com.oreumi.pet_trip_service.model.Enum.Category;
import com.oreumi.pet_trip_service.model.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlaceDTO {

    private Long id;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String categoryCode;

    @NotBlank
    private String categoryName;

    @NotBlank
    private String address;

    private double lat;

    private double lng;

    private String phone;

    private double rating;

    private Integer liked;

    private String homepageUrl;

    private String aiReview;

    private String aiPet;

    private List<String> imageUrls = new ArrayList<>();

    private Long reviewCount;

    public PlaceDTO(Place place){
        this.id = place.getId();
        this.name = place.getName();
        this.categoryName = place.getCategoryName();
        this.categoryCode = place.getCategoryCode();
        this.address = place.getAddress();
        this.lat = place.getLat();
        this.lng = place.getLng();
        this.phone = place.getPhone();
        this.rating = place.getRating();
        this.liked = place.getLiked();
        this.homepageUrl = place.getHomepageUrl();
        this.aiReview = place.getAiReview();
        this.aiPet = place.getAiPet();
    }

    public String getCategoryPath() {
        try {
            Category cat3 = Category.fromCode(this.categoryCode);
            String c1 = Category.getCat1FromCat3(cat3).getDescription();
            String c2 = Category.getCat2FromCat3(cat3).getDescription();
            return c1 + " > " + c2 + " > " + cat3.getDescription();
        } catch (Exception e) {
            return this.categoryName != null ? this.categoryName : "기타";
        }
    }
}