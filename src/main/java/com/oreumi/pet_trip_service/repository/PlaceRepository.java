package com.oreumi.pet_trip_service.repository;

import com.oreumi.pet_trip_service.model.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;   // ✅ spring-data
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    Optional<Place> findByNameAndAddress(String name, String address);
    List<Place> findAllByNameContainingIgnoreCase(String keyword);

    @Query(
            value = """
        select p from Place p
        where (:cat1Like is null or p.categoryCode like :cat1Like)
          and (:kwLike  is null or
               lower(p.name)    like :kwLike
            or lower(p.address) like :kwLike)
        order by
          case
            when :kwLike is null then 0
            when lower(p.name)    like :kwLike then 2
            when lower(p.address) like :kwLike then 1
            else 0
          end desc,
          coalesce(p.rating, 0) desc,
          p.id desc
        """,
            countQuery = """
        select count(p) from Place p
        where (:cat1Like is null or p.categoryCode like :cat1Like)
          and (:kwLike  is null or
               lower(p.name)    like :kwLike
            or lower(p.address) like :kwLike)
        """
    )
    Page<Place> searchByRelevance(@Param("cat1Like") String cat1Like,
                                  @Param("kwLike")   String kwLike,
                                  Pageable pageable);

    // 공통 필터 (정렬은 Pageable Sort로 처리: rating/liked)
    @Query(
            value = """
        select p from Place p
        where (:cat1Like is null or p.categoryCode like :cat1Like)
          and (:kwLike  is null or
               lower(p.name)    like :kwLike
            or lower(p.address) like :kwLike)
        """,
            countQuery = """
        select count(p) from Place p
        where (:cat1Like is null or p.categoryCode like :cat1Like)
          and (:kwLike  is null or
               lower(p.name)    like :kwLike
            or lower(p.address) like :kwLike)
        """
    )
    Page<Place> searchFiltered(@Param("cat1Like") String cat1Like,
                               @Param("kwLike")   String kwLike,
                               Pageable pageable);
}