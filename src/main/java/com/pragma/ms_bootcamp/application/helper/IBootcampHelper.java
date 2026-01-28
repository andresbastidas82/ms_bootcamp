package com.pragma.ms_bootcamp.application.helper;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import reactor.core.publisher.Mono;

public interface IBootcampHelper {

    Mono<BootcampResponse> createBootcamp(BootcampRequest bootcampRequest);
}
