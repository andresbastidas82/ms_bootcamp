package com.pragma.ms_bootcamp.domain.api;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IBootcampServicePort {

    Mono<Bootcamp> save(Bootcamp bootcamp);

    Flux<Bootcamp> getBootcamps(int page, int size, String sortBy, String direction);

    Mono<Long> countBootcamps();
}
