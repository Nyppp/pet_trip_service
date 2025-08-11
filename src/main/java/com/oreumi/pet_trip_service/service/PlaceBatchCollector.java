package com.oreumi.pet_trip_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceBatchCollector {

    private final PlaceDataCollector placeDataCollector;

    // 지역코드
    private static final int[] AREA_CODES = {
            1//서울
//            , 2, 3, 4, 5, 6, 7, 8, //광역시
//            31, 32, 33, 34, 35, 36, 37, 38, 39 //도
    };

    // 콘텐츠 타입
    private static final int[] CONTENT_TYPE_IDS = {
            12//, // 관광지
//            14//, // 문화시설
//            15, // 축제/공연/행사
//            25, // 여행코스
//            28, // 레포츠
//            32, // 숙박
//            38, // 쇼핑
//            39  // 음식점
    };

    public void collectAll() throws Exception {
        for (int areaCode : AREA_CODES) {
            for (int contentTypeId : CONTENT_TYPE_IDS) {
                System.out.printf("==== 수집 시작: 지역코드=%d, 콘텐츠타입=%d ====%n", areaCode, contentTypeId);
                try {
                    placeDataCollector.collectPlaces(areaCode, contentTypeId);
                } catch (Exception e) {
                    System.err.printf("수집 실패: 지역=%d, 콘텐츠=%d, 오류=%s%n",
                            areaCode, contentTypeId, e.getMessage());
                }
                Thread.sleep(500); // API 과부하 방지
            }
        }
    }
}
