package com.oreumi.pet_trip_service.service;

import com.oreumi.pet_trip_service.DTO.PlaceDto;

public interface PlaceService {
    PlaceDto getPlaceDetail(Long placeId);
}