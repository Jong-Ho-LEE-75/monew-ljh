package com.monew.domain.interest.repository;

import com.monew.domain.interest.entity.Interest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

    @EntityGraph(attributePaths = "keywords")
    @Query("""
        select i from Interest i
        where (:cursor is null or i.name > :cursor)
          and (:keyword is null
               or lower(i.name) like lower(concat('%', cast(:keyword as string), '%'))
               or exists (select 1 from InterestKeyword ik
                          where ik.interest = i
                            and lower(ik.keyword) like lower(concat('%', cast(:keyword as string), '%'))))
        order by i.name asc
        """)
    List<Interest> findPageByName(
        @Param("keyword") String keyword,
        @Param("cursor") String cursor,
        Pageable pageable
    );

    @EntityGraph(attributePaths = "keywords")
    @Query("""
        select i from Interest i
        where (:cursor is null or i.subscriberCount < :cursor)
          and (:keyword is null
               or lower(i.name) like lower(concat('%', cast(:keyword as string), '%'))
               or exists (select 1 from InterestKeyword ik
                          where ik.interest = i
                            and lower(ik.keyword) like lower(concat('%', cast(:keyword as string), '%'))))
        order by i.subscriberCount desc, i.name asc
        """)
    List<Interest> findPageBySubscriberCountDesc(
        @Param("keyword") String keyword,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    @EntityGraph(attributePaths = "keywords")
    @Query("""
        select i from Interest i
        where (:cursor is null or i.subscriberCount > :cursor)
          and (:keyword is null
               or lower(i.name) like lower(concat('%', cast(:keyword as string), '%'))
               or exists (select 1 from InterestKeyword ik
                          where ik.interest = i
                            and lower(ik.keyword) like lower(concat('%', cast(:keyword as string), '%'))))
        order by i.subscriberCount asc, i.name asc
        """)
    List<Interest> findPageBySubscriberCountAsc(
        @Param("keyword") String keyword,
        @Param("cursor") Long cursor,
        Pageable pageable
    );

    @EntityGraph(attributePaths = "keywords")
    @Query("""
        select i from Interest i
        where i.id = :id
        """)
    java.util.Optional<Interest> findByIdWithKeywords(@Param("id") UUID id);
}
