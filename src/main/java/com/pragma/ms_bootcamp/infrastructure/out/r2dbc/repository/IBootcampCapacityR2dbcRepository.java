package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository;

import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampCapacityEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface IBootcampCapacityR2dbcRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {

    @Query("SELECT bc.capacity_id FROM bootcamp_capacity bc WHERE bc.bootcamp_id = :bootcampId")
    Flux<Long> findCapacitiesIdsByBootcampId(Long bootcampId);
}
