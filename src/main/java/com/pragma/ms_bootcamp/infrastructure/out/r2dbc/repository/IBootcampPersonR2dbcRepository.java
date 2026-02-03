package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository;

import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampPerson;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface IBootcampPersonR2dbcRepository extends ReactiveCrudRepository<BootcampPerson, Long> {

    Mono<Boolean> existsByBootcampIdAndPersonId(Long bootcampId, Long personId);
}
