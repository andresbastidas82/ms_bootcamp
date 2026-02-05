package com.pragma.ms_bootcamp.domain.usecase;

import com.pragma.ms_bootcamp.domain.exception.BadRequestException;
import com.pragma.ms_bootcamp.domain.exception.NotFoundException;
import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.model.Person;
import com.pragma.ms_bootcamp.domain.spi.IBootcampPersistencePort;
import com.pragma.ms_bootcamp.domain.spi.ICapacityClientPort;
import com.pragma.ms_bootcamp.domain.spi.IPersonClientPort;
import com.pragma.ms_bootcamp.domain.spi.IReportClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootcampUseCaseTest {

    @Mock
    private IBootcampPersistencePort bootcampPersistencePort;
    @Mock
    private ICapacityClientPort capacityClientPort;
    @Mock
    private IReportClientPort reportClientPort;
    @Mock
    private IPersonClientPort personClientPort;

    private BootcampUseCase bootcampUseCase;

    @BeforeEach
    void setUp() {
        bootcampUseCase = new BootcampUseCase(
                bootcampPersistencePort,
                capacityClientPort,
                reportClientPort,
                personClientPort
        );
    }

    @Test
    void save_shouldPersistBootcamp_whenValid() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setId(1L);
        bootcamp.setName("Bootcamp Java");
        bootcamp.setDescription("Desc");
        bootcamp.setLaunchDate(LocalDate.now().plusDays(1));
        bootcamp.setDuration(10);
        bootcamp.setCapacityIds(List.of(1L, 2L));

        when(bootcampPersistencePort.save(any()))
                .thenReturn(Mono.just(bootcamp));

        when(bootcampPersistencePort.getCapacitiesIdsByBootcampId(1L))
                .thenReturn(Flux.just(1L, 2L));

        when(capacityClientPort.getCapacitiesByIds(any()))
                .thenReturn(Flux.empty());

        when(reportClientPort.registredBootcamp(any()))
                .thenReturn(Mono.empty());

        // Act
        Mono<Bootcamp> result = bootcampUseCase.save(bootcamp);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(saved -> saved.getId().equals(1L))
                .verifyComplete();

        verify(bootcampPersistencePort).save(any());
        verify(reportClientPort).registredBootcamp(any());
    }

    @Test
    void save_shouldReturnError_whenNameIsNull() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setDescription("Desc");
        bootcamp.setLaunchDate(LocalDate.now());
        bootcamp.setDuration(10);
        bootcamp.setCapacityIds(List.of(1L));

        // Act
        Mono<Bootcamp> result = bootcampUseCase.save(bootcamp);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();

        verifyNoInteractions(bootcampPersistencePort);
    }

    @Test
    void deleteBootcamp_shouldDeleteBootcamp_whenEverythingIsOk() {
        // Arrange
        Long bootcampId = 1L;

        when(bootcampPersistencePort.getCapacitiesIdsByBootcampId(bootcampId))
                .thenReturn(Flux.just(10L, 20L));

        when(bootcampPersistencePort.findCapacitiesNotReferencedInOtherBootcamps(any(), any()))
                .thenReturn(Flux.just(10L, 20L));

        when(capacityClientPort.deleteCapacities(any()))
                .thenReturn(Mono.just(true));

        when(bootcampPersistencePort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = bootcampUseCase.deleteBootcamp(bootcampId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(bootcampPersistencePort).deleteBootcamp(bootcampId);
    }

    @Test
    void registrationToBootcamp_shouldRegisterSuccessfully() {
        // Arrange
        Long bootcampId = 1L;
        Long personId = 100L;

        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setId(bootcampId);
        bootcamp.setLaunchDate(LocalDate.now());
        bootcamp.setDuration(10);

        when(personClientPort.getPersonById(personId))
                .thenReturn(Mono.just(new Person()));

        when(bootcampPersistencePort.findActiveBootcampsByPersonId(personId))
                .thenReturn(Flux.empty());

        when(bootcampPersistencePort.getBootcampById(bootcampId))
                .thenReturn(Mono.just(bootcamp));

        when(bootcampPersistencePort.registrationToBootcamp(bootcampId, personId))
                .thenReturn(Mono.just(true));

        when(reportClientPort.registredPersonInBootcamp(bootcampId, personId))
                .thenReturn(Mono.empty());

        // Act
        Mono<Boolean> result = bootcampUseCase.registrationToBootcamp(bootcampId, personId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(reportClientPort).registredPersonInBootcamp(bootcampId, personId);
    }

    @Test
    void registrationToBootcamp_shouldFail_whenPersonNotFound() {
        // Arrange
        Long bootcampId = 1L;
        Long personId = 100L;

        when(personClientPort.getPersonById(personId))
                .thenReturn(Mono.empty());

        // Act
        Mono<Boolean> result = bootcampUseCase.registrationToBootcamp(bootcampId, personId);

        // Assert
        StepVerifier.create(result)
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void countBootcamps_shouldReturnTotal() {
        // Arrange
        when(bootcampPersistencePort.countAllBootcamps())
                .thenReturn(Mono.just(5L));

        // Act
        Mono<Long> result = bootcampUseCase.countBootcamps();

        // Assert
        StepVerifier.create(result)
                .expectNext(5L)
                .verifyComplete();
    }

    //error por capacidades repetidas
    @Test
    void save_shouldFail_whenCapacitiesAreRepeated() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setName("Bootcamp Java");
        bootcamp.setDescription("Desc");
        bootcamp.setLaunchDate(LocalDate.now());
        bootcamp.setDuration(10);
        bootcamp.setCapacityIds(List.of(1L, 1L));

        // Act
        Mono<Bootcamp> result = bootcampUseCase.save(bootcamp);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();

        verifyNoInteractions(bootcampPersistencePort);
    }

    //cuando falla el reporte (onErrorResume)
    @Test
    void save_shouldPersistBootcamp_evenIfReportFails() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setId(1L);
        bootcamp.setName("Bootcamp Java");
        bootcamp.setDescription("Desc");
        bootcamp.setLaunchDate(LocalDate.now());
        bootcamp.setDuration(10);
        bootcamp.setCapacityIds(List.of(1L));

        when(bootcampPersistencePort.save(any()))
                .thenReturn(Mono.just(bootcamp));

        when(bootcampPersistencePort.getCapacitiesIdsByBootcampId(1L))
                .thenReturn(Flux.just(1L));

        when(capacityClientPort.getCapacitiesByIds(any()))
                .thenReturn(Flux.empty());

        when(reportClientPort.registredBootcamp(any()))
                .thenReturn(Mono.error(new RuntimeException("Kafka down")));

        // Act
        Mono<Bootcamp> result = bootcampUseCase.save(bootcamp);

        // Assert
        StepVerifier.create(result)
                .expectNext(bootcamp)
                .verifyComplete();
    }

    //sin capacidades para borrar
    @Test
    void deleteBootcamp_shouldDelete_whenNoCapacitiesToDelete() {
        // Arrange
        Long bootcampId = 1L;

        when(bootcampPersistencePort.getCapacitiesIdsByBootcampId(bootcampId))
                .thenReturn(Flux.empty());

        when(bootcampPersistencePort.findCapacitiesNotReferencedInOtherBootcamps(any(), any()))
                .thenReturn(Flux.empty());

        when(bootcampPersistencePort.deleteBootcamp(bootcampId))
                .thenReturn(Mono.just(true));

        // Act
        Mono<Boolean> result = bootcampUseCase.deleteBootcamp(bootcampId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    //falla eliminación de capacidades
    @Test
    void deleteBootcamp_shouldReturnFalse_whenCapacitiesDeletionFails() {
        // Arrange
        Long bootcampId = 1L;

        when(bootcampPersistencePort.getCapacitiesIdsByBootcampId(bootcampId))
                .thenReturn(Flux.just(10L));

        when(bootcampPersistencePort.findCapacitiesNotReferencedInOtherBootcamps(any(), any()))
                .thenReturn(Flux.just(10L));

        when(capacityClientPort.deleteCapacities(any()))
                .thenReturn(Mono.just(false));

        // Act
        Mono<Boolean> result = bootcampUseCase.deleteBootcamp(bootcampId);

        // Assert
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        verify(bootcampPersistencePort, never()).deleteBootcamp(any());
    }

    //máximo 5 bootcamps
    @Test
    void registrationToBootcamp_shouldFail_whenMaxBootcampsReached() {
        // Arrange
        Long personId = 100L;

        when(personClientPort.getPersonById(personId))
                .thenReturn(Mono.just(new Person()));

        when(bootcampPersistencePort.findActiveBootcampsByPersonId(personId))
                .thenReturn(Flux.range(1, 5).map(i -> {
                    Bootcamp b = new Bootcamp();
                    b.setId((long) i);
                    return b;
                }));

        // Act
        Mono<Boolean> result = bootcampUseCase.registrationToBootcamp(10L, personId);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    //bootcamp finalizado
    @Test
    void registrationToBootcamp_shouldFail_whenBootcampFinished() {
        // Arrange
        Long bootcampId = 1L;
        Long personId = 100L;

        Bootcamp finished = new Bootcamp();
        finished.setId(bootcampId);
        finished.setLaunchDate(LocalDate.now().minusDays(20));
        finished.setDuration(5);

        when(personClientPort.getPersonById(personId))
                .thenReturn(Mono.just(new Person()));

        when(bootcampPersistencePort.findActiveBootcampsByPersonId(personId))
                .thenReturn(Flux.empty());

        when(bootcampPersistencePort.getBootcampById(bootcampId))
                .thenReturn(Mono.just(finished));

        // Act
        Mono<Boolean> result = bootcampUseCase.registrationToBootcamp(bootcampId, personId);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    //ya está registrado
    @Test
    void registrationToBootcamp_shouldFail_whenAlreadyRegistered() {
        // Arrange
        Long bootcampId = 1L;
        Long personId = 100L;

        Bootcamp active = new Bootcamp();
        active.setId(bootcampId);

        when(personClientPort.getPersonById(personId))
                .thenReturn(Mono.just(new Person()));

        when(bootcampPersistencePort.findActiveBootcampsByPersonId(personId))
                .thenReturn(Flux.just(active));

        // Act
        Mono<Boolean> result = bootcampUseCase.registrationToBootcamp(bootcampId, personId);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    //fechas solapadas
    @Test
    void registrationToBootcamp_shouldFail_whenDatesOverlap() {
        // Arrange
        Long personId = 100L;

        Bootcamp active = new Bootcamp();
        active.setId(1L);
        active.setLaunchDate(LocalDate.now());
        active.setDuration(10);

        Bootcamp newBootcamp = new Bootcamp();
        newBootcamp.setId(2L);
        newBootcamp.setLaunchDate(LocalDate.now().plusDays(5));
        newBootcamp.setDuration(10);

        when(personClientPort.getPersonById(personId))
                .thenReturn(Mono.just(new Person()));

        when(bootcampPersistencePort.findActiveBootcampsByPersonId(personId))
                .thenReturn(Flux.just(active));

        when(bootcampPersistencePort.getBootcampById(2L))
                .thenReturn(Mono.just(newBootcamp));

        // Act
        Mono<Boolean> result = bootcampUseCase.registrationToBootcamp(2L, personId);

        // Assert
        StepVerifier.create(result)
                .expectError(BadRequestException.class)
                .verify();
    }

    //lista paginada con capacidades
    @Test
    void getBootcamps_shouldReturnBootcampsWithCapacities() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp();
        bootcamp.setId(1L);

        when(bootcampPersistencePort.findAllPaged(anyInt(), anyInt(), any(), any()))
                .thenReturn(Flux.just(bootcamp));

        when(bootcampPersistencePort.getCapacitiesIdsByBootcampId(1L))
                .thenReturn(Flux.empty());

        when(capacityClientPort.getCapacitiesByIds(any()))
                .thenReturn(Flux.empty());

        // Act
        Flux<Bootcamp> result = bootcampUseCase.getBootcamps(0, 10, "id", "ASC");

        // Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }
}
