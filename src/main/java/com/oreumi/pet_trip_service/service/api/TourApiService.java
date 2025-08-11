package com.oreumi.pet_trip_service.service.api;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TourApiService {

    @Value("${tourapi.service-key}")
    private String serviceKey;

    @Value("${tourapi.base-url}")
    private String baseUrl;

    public String callApi(String endpoint, Map<String, String> params) {
        try {
            String encodedKey = URLEncoder.encode(serviceKey, StandardCharsets.UTF_8);

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint)
                    .queryParam("serviceKey", encodedKey)
                    .queryParam("MobileApp", "PetTripApp")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("_type", "json");

            if (params != null) {
                params.forEach(builder::queryParam);
            }

            String url = builder.build(false).toUriString();
            System.out.println("요청 URL: " + url);

            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(URI.create(url), String.class);
        } catch (Exception e) {
            throw new RuntimeException("TourAPI 호출 실패", e);
        }
    }
}