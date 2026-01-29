package com.pragma.ms_bootcamp.domain.spi;

import com.pragma.ms_bootcamp.infrastructure.input.rest.dto.CapacityResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ICapacityClientPort {

    Flux<CapacityResponse> getCapacitiesByIds(List<Long> ids);
}
