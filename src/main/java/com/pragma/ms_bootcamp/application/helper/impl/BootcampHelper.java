package com.pragma.ms_bootcamp.application.helper.impl;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import com.pragma.ms_bootcamp.application.helper.IBootcampHelper;
import com.pragma.ms_bootcamp.application.mapper.IBootcampRequestMapper;
import com.pragma.ms_bootcamp.domain.api.IBootcampServicePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BootcampHelper implements IBootcampHelper {

    private final IBootcampServicePort bootcampServicePort;
    private final IBootcampRequestMapper bootcampRequestMapper;

    @Override
    public Mono<BootcampResponse> createBootcamp(BootcampRequest bootcampRequest) {
        return bootcampServicePort.save(bootcampRequestMapper.toBootcampModel(bootcampRequest))
                .map(bootcampRequestMapper::toBootcampResponse);
    }
}
