package com.pragma.ms_bootcamp.infrastructure.out.client.adapter;

import com.pragma.ms_bootcamp.infrastructure.input.rest.dto.CapacityResponse;
import com.pragma.ms_bootcamp.domain.spi.ICapacityClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CapacityClientAdapter implements ICapacityClientPort {

    private final WebClient webClient;

    public CapacityClientAdapter(@Qualifier("capacityWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Flux<CapacityResponse> getCapacitiesByIds(List<Long> ids) {
        String idsParam = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path("/capacity/byIds")
                                .queryParam("ids", idsParam)
                                .build()
                )
                .retrieve()
                .bodyToFlux(CapacityResponse.class);
    }


}
