package com.pragma.ms_bootcamp.domain.spi;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

public interface IBootcampPersistencePort {

    Mono<Bootcamp> save(Bootcamp bootcamp);
}
