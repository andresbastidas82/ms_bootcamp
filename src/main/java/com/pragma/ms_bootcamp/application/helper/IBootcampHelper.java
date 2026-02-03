package com.pragma.ms_bootcamp.application.helper;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import com.pragma.ms_bootcamp.application.dto.PageResponse;
import com.pragma.ms_bootcamp.application.dto.RegistrationRequest;
import com.pragma.ms_bootcamp.application.dto.RegistrationResponse;
import reactor.core.publisher.Mono;

public interface IBootcampHelper {

    Mono<BootcampResponse> createBootcamp(BootcampRequest bootcampRequest);

    Mono<PageResponse<BootcampResponse>> getBootcamps(int page, int size, String sortBy, String direction);

    Mono<Boolean> deleteBootcamp(Long bootcampId);

    Mono<RegistrationResponse> registrationToBootcamp(RegistrationRequest request);
}
