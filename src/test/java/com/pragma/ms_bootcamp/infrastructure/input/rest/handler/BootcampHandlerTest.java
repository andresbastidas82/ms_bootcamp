package com.pragma.ms_bootcamp.infrastructure.input.rest.handler;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import com.pragma.ms_bootcamp.application.dto.PageResponse;
import com.pragma.ms_bootcamp.application.dto.RegistrationRequest;
import com.pragma.ms_bootcamp.application.dto.RegistrationResponse;
import com.pragma.ms_bootcamp.application.helper.IBootcampHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampHandlerTest {

    @Mock
    private IBootcampHelper bootcampHelper;

    @InjectMocks
    private BootcampHandler bootcampHandler;

    // --- CREATE BOOTCAMP ---

    @Test
    @DisplayName("Create Bootcamp: Should return 200 OK when successful")
    void createBootcamp_ShouldReturnOk() {
        // Arrange
        BootcampRequest requestDto = new BootcampRequest();

        // Simulamos un objeto de respuesta genérico (puede ser tu DTO de respuesta o el mismo objeto)
        BootcampResponse responseDto = new BootcampResponse();

        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(requestDto));

        when(bootcampHelper.createBootcamp(any(BootcampRequest.class)))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<ServerResponse> result = bootcampHandler.createBootcamp(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(bootcampHelper).createBootcamp(any(BootcampRequest.class));
    }

    // --- LIST BOOTCAMPS ---

    @Test
    @DisplayName("List Bootcamps: Should use default params and return 200 OK")
    void listBootcamps_WithDefaultParams_ShouldReturnOk() {
        // Arrange
        MockServerRequest request = MockServerRequest.builder()
                // No enviamos query params para probar los defaults (0, 10, name, asc)
                .build();

        PageResponse responseDto = new PageResponse();

        when(bootcampHelper.getBootcamps(0, 10, "name", "asc"))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<ServerResponse> result = bootcampHandler.listBootcamps(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    @Test
    @DisplayName("List Bootcamps: Should use provided params and return 200 OK")
    void listBootcamps_WithCustomParams_ShouldReturnOk() {
        // Arrange
        MockServerRequest request = MockServerRequest.builder()
                .queryParam("page", "1")
                .queryParam("size", "5")
                .queryParam("sortBy", "date")
                .queryParam("direction", "desc")
                .build();

        PageResponse responseDto = new PageResponse();

        when(bootcampHelper.getBootcamps(1, 5, "date", "desc"))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<ServerResponse> result = bootcampHandler.listBootcamps(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    // --- DELETE BOOTCAMP ---

    @Test
    @DisplayName("Delete Bootcamp: Should return 200 OK with true when successful")
    void deleteBootcamp_ShouldReturnOk() {
        // Arrange
        Long bootcampId = 1L;
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", String.valueOf(bootcampId))
                .build();

        when(bootcampHelper.deleteBootcamp(bootcampId))
                .thenReturn(Mono.just(true));

        // Act
        Mono<ServerResponse> result = bootcampHandler.deleteBootcamp(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(bootcampHelper).deleteBootcamp(bootcampId);
    }

    @Test
    @DisplayName("Delete Bootcamp: Should return 200 OK with false when error occurs")
    void deleteBootcamp_OnError_ShouldReturnFalse() {
        // Arrange
        Long bootcampId = 1L;
        MockServerRequest request = MockServerRequest.builder()
                .pathVariable("id", String.valueOf(bootcampId))
                .build();

        // Simulamos un error en el helper
        when(bootcampHelper.deleteBootcamp(bootcampId))
                .thenReturn(Mono.error(new RuntimeException("Error interno")));

        // Act
        Mono<ServerResponse> result = bootcampHandler.deleteBootcamp(request);

        // Assert
        // El handler tiene .onErrorReturn(false), por lo que debe responder OK con body=false
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    // --- REGISTRATION TO BOOTCAMP ---

    @Test
    @DisplayName("Registration: Should return 200 OK when successful")
    void registrationToBootcamp_ShouldReturnOk() {
        // Arrange
        RegistrationRequest registrationRequest = new RegistrationRequest();
        // setear valores si es necesario

        RegistrationResponse responseDto = new RegistrationResponse();

        MockServerRequest request = MockServerRequest.builder()
                .body(Mono.just(registrationRequest));

        when(bootcampHelper.registrationToBootcamp(any(RegistrationRequest.class)))
                .thenReturn(Mono.just(responseDto));

        // Act
        Mono<ServerResponse> result = bootcampHandler.registrationToBootcamp(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();

        verify(bootcampHelper).registrationToBootcamp(any(RegistrationRequest.class));
    }
}