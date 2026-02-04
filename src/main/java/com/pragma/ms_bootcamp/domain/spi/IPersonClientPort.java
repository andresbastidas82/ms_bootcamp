package com.pragma.ms_bootcamp.domain.spi;

import com.pragma.ms_bootcamp.domain.model.Person;
import reactor.core.publisher.Mono;

public interface IPersonClientPort {

    Mono<Person> getPersonById(Long personId);
}
