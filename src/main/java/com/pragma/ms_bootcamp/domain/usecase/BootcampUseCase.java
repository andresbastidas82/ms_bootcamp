package com.pragma.ms_bootcamp.domain.usecase;

import com.pragma.ms_bootcamp.domain.api.IBootcampServicePort;
import com.pragma.ms_bootcamp.domain.exception.BadRequestException;
import com.pragma.ms_bootcamp.domain.exception.NotFoundException;
import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.model.Capacity;
import com.pragma.ms_bootcamp.domain.model.Technology;
import com.pragma.ms_bootcamp.domain.spi.IBootcampPersistencePort;
import com.pragma.ms_bootcamp.domain.spi.ICapacityClientPort;
import com.pragma.ms_bootcamp.domain.spi.IPersonClientPort;
import com.pragma.ms_bootcamp.domain.spi.IReportClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.pragma.ms_bootcamp.domain.utils.Constants.ALREADY_REGISTERED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.BOOTCAMP_FINISHED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.BOOTCAMP_NOT_FOUND;
import static com.pragma.ms_bootcamp.domain.utils.Constants.DESCRIPTION_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.DURATION_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.INVALID_CAPACITY_SIZE;
import static com.pragma.ms_bootcamp.domain.utils.Constants.INVALID_MAX_5_BOOTCAMP;
import static com.pragma.ms_bootcamp.domain.utils.Constants.INVALID_REGISTERED_BOOTCAMP;
import static com.pragma.ms_bootcamp.domain.utils.Constants.LAUNCH_DATE_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.NAME_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.PERSON_NOT_FOUND;
import static com.pragma.ms_bootcamp.domain.utils.Constants.REPEATED_CAPACITY;

@Service
@RequiredArgsConstructor
@Slf4j
public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;
    private final ICapacityClientPort capacityClientPort;
    private final IReportClientPort reportClientPort;
    private final IPersonClientPort personClientPort;

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return validateBusinessRules(bootcamp)
                .then(Mono.defer(() -> bootcampPersistencePort.save(bootcamp)))
                .flatMap(saved ->
                        getBootcampWitchCapacities(saved)
                                .flatMap(result ->
                                        reportClientPort
                                                .registredBootcamp(result)
                                                .onErrorResume(error -> {
                                                    log.error("Error registrando bootcamp en reporte {}", error.getMessage());
                                                    return Mono.empty();
                                                })
                                                .thenReturn(saved)
                                )
                );
    }

    @Override
    public Flux<Bootcamp> getBootcamps(int page, int size, String sortBy, String direction) {
        return bootcampPersistencePort.findAllPaged(page, size, sortBy, direction)
                .flatMap(this::getBootcampWitchCapacities);
    }

    @Override
    public Mono<Long> countBootcamps() {
        return bootcampPersistencePort.countAllBootcamps();
    }

    @Override
    public Mono<Boolean> deleteBootcamp(Long bootcampId) {
        //Buscar capacidades y determinar si estan referenciadas en otros bootcamp
        return bootcampPersistencePort.getCapacitiesIdsByBootcampId(bootcampId)
                .collectList()
                .flatMapMany(capacityIds ->
                    bootcampPersistencePort.findCapacitiesNotReferencedInOtherBootcamps(bootcampId, capacityIds)
                )
                .collectList()
                .flatMap(capacityIds -> {
                    if(!capacityIds.isEmpty()) {
                        return capacityClientPort.deleteCapacities(capacityIds);
                    }
                    return Mono.just(true);
                })
                .flatMap(result -> {
                    if(Boolean.TRUE.equals(result)) {
                        return bootcampPersistencePort.deleteBootcamp(bootcampId);
                    }
                    return Mono.just(false);
                });
    }

    @Override
    public Mono<Boolean> registrationToBootcamp(Long bootcampId, Long personId) {
        return validateRegistration(bootcampId, personId)
                .then(Mono.defer(() -> bootcampPersistencePort.registrationToBootcamp(bootcampId, personId)))
                .flatMap(result -> {
                    if (Boolean.TRUE.equals(result)) {
                        return reportClientPort
                                .registredPersonInBootcamp(bootcampId, personId)
                                .onErrorResume(error -> {
                                    log.error("Error enviando reporte {}", error.getMessage());
                                    return Mono.empty();
                                })
                                .thenReturn(true);
                    }
                    return Mono.just(false);
                });
    }

    private Mono<Void> validateRegistration(Long bootcampId, Long personId) {
        return personClientPort.getPersonById(personId)
                .switchIfEmpty(Mono.error(new NotFoundException(PERSON_NOT_FOUND)))
                .flatMap(person ->
                        bootcampPersistencePort
                        .findActiveBootcampsByPersonId(personId)
                        .collectList()
                        .flatMap(activeBootcamps -> {
                            // 1️⃣ Ya registrado
                            if (activeBootcamps.stream().anyMatch(b -> b.getId().equals(bootcampId))) {
                                return Mono.error(new BadRequestException(ALREADY_REGISTERED));
                            }
                            // 2️⃣ Máximo 5 bootcamps
                            if (activeBootcamps.size() >= 5) {
                                return Mono.error(new BadRequestException(INVALID_MAX_5_BOOTCAMP));
                            }
                            // 3️⃣ Obtener bootcamp a registrar
                            return bootcampPersistencePort.getBootcampById(bootcampId)
                                    .switchIfEmpty(Mono.error(new NotFoundException(BOOTCAMP_NOT_FOUND)))
                                    .flatMap(newBootcamp -> {
                                        LocalDate currentDate = LocalDate.now();
                                        if (newBootcamp.getLaunchDate().plusDays(newBootcamp.getDuration()).isBefore(currentDate)) {
                                            return Mono.error(new BadRequestException(BOOTCAMP_FINISHED));
                                        }
                                        // 4️⃣ Validar solapamiento de fechas
                                        boolean overlaps = activeBootcamps.stream().anyMatch(existing -> {
                                            LocalDate existingStart = existing.getLaunchDate();
                                            LocalDate existingEnd = existingStart.plusDays(existing.getDuration());
                                            LocalDate newStart = newBootcamp.getLaunchDate();
                                            return !newStart.isAfter(existingEnd) && !newStart.isBefore(existingStart);
                                        });

                                        if (overlaps) {
                                            return Mono.error(new BadRequestException(INVALID_REGISTERED_BOOTCAMP));
                                        }
                                        return Mono.empty();
                                    });
                        })
                );

    }


    private Mono<Bootcamp> getBootcampWitchCapacities(Bootcamp bootcamp) {
        return bootcampPersistencePort
                .getCapacitiesIdsByBootcampId(bootcamp.getId())
                .collectList()
                .flatMap(capacitiesIds ->
                    capacityClientPort.getCapacitiesByIds(capacitiesIds)
                        .map(item -> new Capacity(item.getId(), item.getName(), item.getDescription(),
                                item.getTechnologies().stream().map(e -> new Technology(e.getId(), e.getName())).toList()))
                        .collectList()
                        .map(capacities -> {
                            bootcamp.setCapacities(capacities);
                            return bootcamp;
                        })
                );
    }

    private Mono<Void> validateBusinessRules(Bootcamp bootcamp) {
        List<String> errors = new ArrayList<>();
        if (bootcamp.getName() == null || bootcamp.getName().isEmpty()) {
            errors.add(NAME_IS_REQUIRED);
        }
        if (bootcamp.getDescription() == null || bootcamp.getDescription().isEmpty()) {
            errors.add(DESCRIPTION_IS_REQUIRED);
        }
        if (bootcamp.getLaunchDate() == null) {
            errors.add(LAUNCH_DATE_IS_REQUIRED);
        }
        if(bootcamp.getDuration() == null) {
            errors.add(DURATION_IS_REQUIRED);
        }
        if (bootcamp.getCapacityIds() == null || bootcamp.getCapacityIds().isEmpty() || bootcamp.getCapacityIds().size() > 4) {
            errors.add(INVALID_CAPACITY_SIZE);
        }

        long distinct = bootcamp.getCapacityIds() != null
                ? bootcamp.getCapacityIds().stream().distinct().count() : 0L;
        if (bootcamp.getCapacityIds() != null && !bootcamp.getCapacityIds().isEmpty()
                && distinct != bootcamp.getCapacityIds().size()) {
            errors.add(REPEATED_CAPACITY);
        }
        if (!errors.isEmpty()) {
            return Mono.error(new BadRequestException(String.join("|", errors)));
        }

        return Mono.empty();
    }
}
