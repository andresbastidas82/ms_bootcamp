package com.pragma.ms_bootcamp.domain.usecase;

import com.pragma.ms_bootcamp.domain.api.IBootcampServicePort;
import com.pragma.ms_bootcamp.domain.exception.InvalidBootcampException;
import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.spi.IBootcampPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static com.pragma.ms_bootcamp.domain.utils.Constants.INVALID_CAPACITY_SIZE;
import static com.pragma.ms_bootcamp.domain.utils.Constants.REPEATED_CAPACITY;

@Service
@RequiredArgsConstructor
public class BootcampUseCase implements IBootcampServicePort {

    private final IBootcampPersistencePort bootcampPersistencePort;

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return validateBusinessRules(bootcamp)
                .then(bootcampPersistencePort.save(bootcamp));
    }

    private Mono<Void> validateBusinessRules(Bootcamp bootcamp) {
        if (bootcamp.getCapacityIds() == null ||
                bootcamp.getCapacityIds().isEmpty() ||
                bootcamp.getCapacityIds().size() > 4) {
            return Mono.error(new InvalidBootcampException(INVALID_CAPACITY_SIZE));
        }

        long distinct = bootcamp.getCapacityIds().stream().distinct().count();
        if (distinct != bootcamp.getCapacityIds().size()) {
            return Mono.error(new InvalidBootcampException(REPEATED_CAPACITY));
        }

        return Mono.empty();
    }
}
