package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.adapter;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.infrastructure.exception.InvalidSortFieldException;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampCapacityEntity;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampEntity;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampPerson;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.mapper.IBootcampEntityMapper;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.projections.BootcampWitchCapacityCountProjection;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampCapacityR2dbcRepository;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampPersonR2dbcRepository;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampR2dbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BootcampRepositoryAdapterTest {

    @Mock
    private IBootcampR2dbcRepository bootcampR2dbcRepository;
    @Mock
    private IBootcampCapacityR2dbcRepository bootcampCapacityR2dbcRepository;
    @Mock
    private IBootcampPersonR2dbcRepository bootcampPersonR2dbcRepository;
    @Mock
    private IBootcampEntityMapper bootcampEntityMapper;
    @Mock
    private DatabaseClient databaseClient;
    @Mock
    private DatabaseClient.GenericExecuteSpec genericExecuteSpec;
    @Mock
    private RowsFetchSpec<BootcampWitchCapacityCountProjection> rowsFetchSpec;

    private BootcampRepositoryAdapter bootcampRepositoryAdapter;

    @BeforeEach
    void setUp() {
        bootcampRepositoryAdapter = new BootcampRepositoryAdapter(
                bootcampR2dbcRepository,
                bootcampCapacityR2dbcRepository,
                bootcampPersonR2dbcRepository,
                bootcampEntityMapper,
                databaseClient
        );
    }

    @Test
    @DisplayName("Save: Should map, save entities, and map back to model")
    void save_ShouldPersistAndReturnModel() {
        // Arrange
        Bootcamp bootcampToSave = new Bootcamp();
        bootcampToSave.setCapacityIds(List.of(10L, 20L));

        BootcampEntity bootcampEntity = new BootcampEntity();
        bootcampEntity.setId(1L);
        bootcampEntity.setCapacityIds(List.of(10L, 20L));

        Bootcamp savedBootcampModel = new Bootcamp();
        savedBootcampModel.setId(1L);

        when(bootcampEntityMapper.toBootcampEntity(bootcampToSave)).thenReturn(bootcampEntity);
        when(bootcampR2dbcRepository.save(bootcampEntity)).thenReturn(Mono.just(bootcampEntity));
        when(bootcampCapacityR2dbcRepository.save(any(BootcampCapacityEntity.class)))
                .thenReturn(Mono.just(new BootcampCapacityEntity()));
        when(bootcampEntityMapper.toBootcampModel(bootcampEntity)).thenReturn(savedBootcampModel);

        // Act
        Mono<Bootcamp> result = bootcampRepositoryAdapter.save(bootcampToSave);

        // Assert
        StepVerifier.create(result)
                .expectNext(savedBootcampModel)
                .verifyComplete();

        verify(bootcampR2dbcRepository).save(bootcampEntity);
        verify(bootcampCapacityR2dbcRepository, times(2)).save(any(BootcampCapacityEntity.class));
    }

    @Test
    @DisplayName("FindAllPaged: Should execute correctly formatted query and map projections")
    void findAllPaged_ShouldReturnPagedBootcamps() {
        // Arrange
        int page = 0;
        int size = 10;
        String sortBy = "name";
        String direction = "asc";

        BootcampWitchCapacityCountProjection projection = new BootcampWitchCapacityCountProjection(1L, "name", "desc", LocalDate.now(), 10, 5L);
        Bootcamp bootcampModel = new Bootcamp();
        bootcampModel.setId(1L);

        // Mocking the DatabaseClient fluent API
        when(databaseClient.sql(anyString())).thenReturn(genericExecuteSpec);

        // --- CORRECCIÓN AQUÍ ---
        // Se especifica que el 'any()' debe ser del tipo BiFunction para resolver la ambigüedad.
        when(genericExecuteSpec.map(any(BiFunction.class))).thenReturn(rowsFetchSpec);

        when(rowsFetchSpec.all()).thenReturn(Flux.just(projection));
        when(bootcampEntityMapper.toModelProjection(projection)).thenReturn(bootcampModel);

        // Act
        Flux<Bootcamp> result = bootcampRepositoryAdapter.findAllPaged(page, size, sortBy, direction);

        // Assert
        StepVerifier.create(result)
                .expectNext(bootcampModel)
                .verifyComplete();

        // Capturar el argumento SQL para verificar su contenido.
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(databaseClient).sql(sqlCaptor.capture());
        String capturedSql = sqlCaptor.getValue();

        // Verificar que la paginación y el ordenamiento son correctos en la consulta.
        assertTrue(capturedSql.contains("ORDER BY b.name ASC"));
        assertTrue(capturedSql.contains("LIMIT 10 OFFSET 0"));
    }

    @Test
    @DisplayName("FindAllPaged: Should throw exception for invalid sort field")
    void findAllPaged_WhenInvalidSort_ShouldThrowException() {
        // Arrange
        String invalidSortBy = "invalidField";

        // Act & Assert
        assertThrows(InvalidSortFieldException.class, () -> {
            bootcampRepositoryAdapter.findAllPaged(0, 10, invalidSortBy, "asc");
        });
    }

    @Test
    @DisplayName("GetById: Should return model when entity is found")
    void getBootcampById_WhenFound_ShouldReturnModel() {
        // Arrange
        Long bootcampId = 1L;
        BootcampEntity foundEntity = new BootcampEntity();
        Bootcamp expectedModel = new Bootcamp();

        when(bootcampR2dbcRepository.findById(bootcampId)).thenReturn(Mono.just(foundEntity));
        when(bootcampEntityMapper.toBootcampModel(foundEntity)).thenReturn(expectedModel);

        // Act
        Mono<Bootcamp> result = bootcampRepositoryAdapter.getBootcampById(bootcampId);

        // Assert
        StepVerifier.create(result)
                .expectNext(expectedModel)
                .verifyComplete();
    }

    @Test
    @DisplayName("GetById: Should return empty when entity is not found")
    void getBootcampById_WhenNotFound_ShouldReturnEmpty() {
        // Arrange
        Long bootcampId = 99L;
        when(bootcampR2dbcRepository.findById(bootcampId)).thenReturn(Mono.empty());

        // Act
        Mono<Bootcamp> result = bootcampRepositoryAdapter.getBootcampById(bootcampId);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(bootcampEntityMapper, never()).toBootcampModel(any());
    }

    @Test
    @DisplayName("DeleteBootcamp: Should delete relations and then the bootcamp")
    @Transactional
    void deleteBootcamp_ShouldDeleteAllRelatedData() {
        // Arrange
        Long bootcampId = 1L;
        when(bootcampCapacityR2dbcRepository.deleteAllByBootcampId(bootcampId)).thenReturn(Mono.empty());
        when(bootcampR2dbcRepository.deleteById(bootcampId)).thenReturn(Mono.empty());

        // Act
        Mono<Boolean> result = bootcampRepositoryAdapter.deleteBootcamp(bootcampId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(bootcampCapacityR2dbcRepository).deleteAllByBootcampId(bootcampId);
        verify(bootcampR2dbcRepository).deleteById(bootcampId);
    }

    @Test
    @DisplayName("RegistrationToBootcamp: Should save enrollment and return true")
    void registrationToBootcamp_ShouldSaveAndReturnTrue() {
        // Arrange
        Long bootcampId = 1L;
        Long personId = 100L;
        BootcampPerson savedEnrollment = new BootcampPerson(bootcampId, personId, null);

        when(bootcampPersonR2dbcRepository.save(any(BootcampPerson.class)))
                .thenReturn(Mono.just(savedEnrollment));

        // Act
        Mono<Boolean> result = bootcampRepositoryAdapter.registrationToBootcamp(bootcampId, personId);

        // Assert
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        verify(bootcampPersonR2dbcRepository).save(any(BootcampPerson.class));
    }

    @Test
    @DisplayName("FindActiveBootcampsByPersonId: Should return flux of bootcamps when found")
    void findActiveBootcampsByPersonId_ShouldReturnFluxOfBootcamps() {
        // Arrange
        Long personId = 123L;
        Bootcamp activeBootcamp = new Bootcamp();
        activeBootcamp.setId(1L);
        activeBootcamp.setName("Bootcamp Activo");

        // Simulamos que el repositorio encuentra un bootcamp activo para la persona
        // Usamos any(LocalDate.class) porque LocalDate.now() es difícil de predecir en un test
        when(bootcampR2dbcRepository.findActiveBootcampsByPersonId(any(LocalDate.class), eq(personId)))
                .thenReturn(Flux.just(activeBootcamp));

        // Act
        Flux<Bootcamp> result = bootcampRepositoryAdapter.findActiveBootcampsByPersonId(personId);

        // Assert
        StepVerifier.create(result)
                .expectNext(activeBootcamp) // Verificamos que el bootcamp se emite
                .verifyComplete();

        // Verificamos que se llamó al metodo del repositorio con los parámetros correctos
        verify(bootcampR2dbcRepository).findActiveBootcampsByPersonId(any(LocalDate.class), eq(personId));
    }

    @Test
    @DisplayName("FindActiveBootcampsByPersonId: Should return empty flux when none are found")
    void findActiveBootcampsByPersonId_WhenNoneFound_ShouldReturnEmptyFlux() {
        // Arrange
        Long personId = 456L;

        // Simulamos que el repositorio no encuentra bootcamps y devuelve un Flux vacío
        when(bootcampR2dbcRepository.findActiveBootcampsByPersonId(any(LocalDate.class), eq(personId)))
                .thenReturn(Flux.empty());

        // Act
        Flux<Bootcamp> result = bootcampRepositoryAdapter.findActiveBootcampsByPersonId(personId);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0) // Verificamos que no se emite ningún elemento
                .verifyComplete();

        verify(bootcampR2dbcRepository).findActiveBootcampsByPersonId(any(LocalDate.class), eq(personId));
    }

    @Test
    @DisplayName("FindCapacitiesNotReferenced: Should return IDs returned by repository")
    void findCapacitiesNotReferencedInOtherBootcamps_ShouldReturnIds() {
        // Arrange
        Long bootcampId = 1L;
        List<Long> idsToCheck = List.of(10L, 20L, 30L);
        // Simulamos que el repositorio devuelve solo el 10 y el 30 (el 20 estaría referenciado en otro lado)
        List<Long> expectedIds = List.of(10L, 30L);

        when(bootcampCapacityR2dbcRepository.findCapacitiesNotReferencedInOtherBootcamps(idsToCheck, bootcampId))
                .thenReturn(Flux.fromIterable(expectedIds));

        // Act
        Flux<Long> result = bootcampRepositoryAdapter.findCapacitiesNotReferencedInOtherBootcamps(bootcampId, idsToCheck);

        // Assert
        StepVerifier.create(result)
                .expectNext(10L)
                .expectNext(30L)
                .verifyComplete();

        verify(bootcampCapacityR2dbcRepository).findCapacitiesNotReferencedInOtherBootcamps(idsToCheck, bootcampId);
    }

    @Test
    @DisplayName("FindCapacitiesNotReferenced: Should return empty when repository returns empty")
    void findCapacitiesNotReferencedInOtherBootcamps_ShouldReturnEmpty() {
        // Arrange
        Long bootcampId = 1L;
        List<Long> idsToCheck = List.of(20L);

        when(bootcampCapacityR2dbcRepository.findCapacitiesNotReferencedInOtherBootcamps(idsToCheck, bootcampId))
                .thenReturn(Flux.empty());

        // Act
        Flux<Long> result = bootcampRepositoryAdapter.findCapacitiesNotReferencedInOtherBootcamps(bootcampId, idsToCheck);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();

        verify(bootcampCapacityR2dbcRepository).findCapacitiesNotReferencedInOtherBootcamps(idsToCheck, bootcampId);
    }
}