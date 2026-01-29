package com.pragma.ms_bootcamp.domain.usecase;

import com.pragma.ms_bootcamp.domain.api.IBootcampServicePort;
import com.pragma.ms_bootcamp.domain.exception.BadRequestException;
import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.model.Capacity;
import com.pragma.ms_bootcamp.domain.model.Technology;
import com.pragma.ms_bootcamp.domain.spi.IBootcampPersistencePort;
import com.pragma.ms_bootcamp.domain.spi.ICapacityClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.pragma.ms_bootcamp.domain.utils.Constants.DESCRIPTION_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.DURATION_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.INVALID_CAPACITY_SIZE;
import static com.pragma.ms_bootcamp.domain.utils.Constants.LAUNCH_DATE_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.NAME_IS_REQUIRED;
import static com.pragma.ms_bootcamp.domain.utils.Constants.REPEATED_CAPACITY;

@Service
@RequiredArgsConstructor
public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;
    private final ICapacityClientPort capacityClientPort;

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return validateBusinessRules(bootcamp)
                .then(bootcampPersistencePort.save(bootcamp));
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
