package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository;

import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampCapacityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IBootcampCapacityR2dbcRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {
}
