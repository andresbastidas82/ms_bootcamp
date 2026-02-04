package com.pragma.ms_bootcamp.infrastructure.out.client.adapter;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.spi.IReportClientPort;
import com.pragma.ms_bootcamp.infrastructure.input.rest.dto.ReportResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReportClientAdapter implements IReportClientPort {

    private final WebClient webClient;

    public ReportClientAdapter(@Qualifier("reportWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> registredBootcamp(Bootcamp bootcamp) {
        return webClient.post()
                .uri("/report-service")
                .bodyValue(bootcamp)
                .retrieve()
                .bodyToMono(ReportResponse.class)
                .doOnNext(response -> log.info(response.getMessage()))
                .doOnError(error -> log.error("❌ Error al registrar bootcamp en report-service ➡️ {}", error.getMessage()))
                .then();
    }

    @Override
    public Mono<Void> registredPersonInBootcamp(Long bootcampId, Long personId) {
        return webClient.get()
                .uri(uriBuilder ->
                    uriBuilder
                            .path("/report-service")
                            .queryParam("bootcampId", bootcampId)
                            .queryParam("personId", personId)
                            .build()
                )
                .retrieve()
                .bodyToMono(ReportResponse.class)
                .doOnNext(response -> log.info(response.getMessage()))
                .doOnError(error -> log.error("❌ Error al registrar persona en bootcamp ➡️ {}", error.getMessage()))
                .then();
    }
}
