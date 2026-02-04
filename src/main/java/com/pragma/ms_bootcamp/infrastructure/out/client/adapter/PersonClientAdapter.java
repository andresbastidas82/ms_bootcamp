package com.pragma.ms_bootcamp.infrastructure.out.client.adapter;

import com.pragma.ms_bootcamp.domain.exception.NotFoundException;
import com.pragma.ms_bootcamp.domain.model.Person;
import com.pragma.ms_bootcamp.domain.spi.IPersonClientPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.pragma.ms_bootcamp.domain.utils.Constants.PERSON_NOT_FOUND;

@Component
@Slf4j
public class PersonClientAdapter implements IPersonClientPort {

    private final WebClient webClient;

    public PersonClientAdapter(@Qualifier("personWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Person> getPersonById(Long id) {
        return webClient.get()
                .uri("/person/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new NotFoundException(PERSON_NOT_FOUND)))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new RuntimeException("Error en el servicio de personas")))
                .bodyToMono(Person.class);
    }
}
