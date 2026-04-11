package com.monew.domain.interest.repository;

import com.monew.domain.interest.entity.Interest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, UUID> {

    @Query("""
        select distinct i from Interest i
        left join fetch i.keywords
        where (:cursor is null or i.name > :cursor)
        order by i.name asc
        """)
    List<Interest> findPage(@Param("cursor") String cursor, Pageable pageable);

    @Query("""
        select distinct i from Interest i
        left join fetch i.keywords
        where i.id = :id
        """)
    java.util.Optional<Interest> findByIdWithKeywords(@Param("id") UUID id);
}
