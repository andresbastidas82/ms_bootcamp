package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository;

import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampCapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IBootcampCapacityR2dbcRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {

    @Query("SELECT bc.capacity_id FROM bootcamp_capacity bc WHERE bc.bootcamp_id = :bootcampId")
    Flux<Long> findCapacitiesIdsByBootcampId(Long bootcampId);

    @Query("""
        SELECT bc.capacity_id
        FROM bootcamp_capacity bc
        WHERE bc.capacity_id IN (:capacityIds)
        GROUP BY bc.capacity_id
        HAVING COUNT(DISTINCT bc.bootcamp_id) = 1
           AND MAX(bc.bootcamp_id) = :bootcampId
    """)
    Flux<Long> findCapacitiesNotReferencedInOtherBootcamps(
            @Param("capacityIds") List<Long> capacityIds,
            @Param("bootcampId") Long bootcampId
    );

    Mono<Void> deleteAllByBootcampId(Long bootcampId);
}
