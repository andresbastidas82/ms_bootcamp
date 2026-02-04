package com.pragma.ms_bootcamp.domain.spi;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

public interface IReportClientPort {

    Mono<Void> registredBootcamp(Bootcamp bootcamp);

    Mono<Void> registredPersonInBootcamp(Long bootcampId, Long personId);
}
