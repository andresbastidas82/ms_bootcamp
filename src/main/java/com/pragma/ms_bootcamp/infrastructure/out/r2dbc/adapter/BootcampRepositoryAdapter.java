package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.adapter;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.spi.IBootcampPersistencePort;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampCapacityEntity;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.mapper.IBootcampEntityMapper;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampCapacityR2dbcRepository;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampR2dbcRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class BootcampRepositoryAdapter implements IBootcampPersistencePort {

    private final IBootcampR2dbcRepository bootcampR2dbcRepository;
    private final IBootcampCapacityR2dbcRepository bootcampCapacityR2dbcRepository;
    private final IBootcampEntityMapper bootcampEntityMapper;

    public BootcampRepositoryAdapter(IBootcampR2dbcRepository bootcampR2dbcRepository,
                                     IBootcampCapacityR2dbcRepository bootcampCapacityR2dbcRepository,
                                     IBootcampEntityMapper bootcampEntityMapper) {
        this.bootcampR2dbcRepository = bootcampR2dbcRepository;
        this.bootcampCapacityR2dbcRepository = bootcampCapacityR2dbcRepository;
        this.bootcampEntityMapper = bootcampEntityMapper;
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return Mono.fromSupplier(() -> bootcamp)
                .map(bootcampEntityMapper::toBootcampEntity)
                .flatMap(bootcampR2dbcRepository::save)
                .flatMap(bootcampSaved ->
                        Flux.fromIterable(bootcampSaved.getCapacityIds())
                                .map(capacityId -> new BootcampCapacityEntity(bootcampSaved.getId(), capacityId))
                                .flatMap(bootcampCapacityR2dbcRepository::save)
                                .then(Mono.just(bootcampSaved))
                )
                .map(bootcampEntityMapper::toBootcampModel);
    }
}
