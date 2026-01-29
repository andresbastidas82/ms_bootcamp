package com.pragma.ms_bootcamp.application.helper;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import com.pragma.ms_bootcamp.application.dto.PageResponse;
import reactor.core.publisher.Mono;

public interface IBootcampHelper {

    Mono<BootcampResponse> createBootcamp(BootcampRequest bootcampRequest);

    Mono<PageResponse<BootcampResponse>> getBootcamps(int page, int size, String sortBy, String direction);
}
