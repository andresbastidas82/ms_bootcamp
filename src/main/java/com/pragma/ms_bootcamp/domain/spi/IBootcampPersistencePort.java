package com.pragma.ms_bootcamp.domain.spi;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IBootcampPersistencePort {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Flux<Bootcamp> findAllPaged(int page, int size, String sortBy, String direction);

    Mono<Long> countAllBootcamps();

    Flux<Long> getCapacitiesIdsByBootcampId(Long bootcampId);

    Mono<Bootcamp> getBootcampById(Long bootcampId);

    Mono<Boolean> deleteBootcamp(Long bootcampId);

    Flux<Long> findCapacitiesNotReferencedInOtherBootcamps(Long bootcampId, List<Long> ids);
}
