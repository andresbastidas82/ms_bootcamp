package com.pragma.ms_bootcamp.infrastructure.input.rest;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import com.pragma.ms_bootcamp.application.helper.IBootcampHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bootcamps")
public class BootcampController {

    private final IBootcampHelper bootcampHelper;

    public BootcampController(IBootcampHelper bootcampHelper) {
        this.bootcampHelper = bootcampHelper;
    }

    @PostMapping
    public Mono<ResponseEntity<BootcampResponse>> createBootcamp(@Valid @RequestBody BootcampRequest bootcampRequest) {
        return bootcampHelper.createBootcamp(bootcampRequest).map(ResponseEntity::ok);
    }
}
