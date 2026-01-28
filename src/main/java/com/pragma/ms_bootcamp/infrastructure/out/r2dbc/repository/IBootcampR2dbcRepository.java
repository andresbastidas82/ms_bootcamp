package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository;

import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IBootcampR2dbcRepository extends ReactiveCrudRepository<BootcampEntity, Long> {
}
