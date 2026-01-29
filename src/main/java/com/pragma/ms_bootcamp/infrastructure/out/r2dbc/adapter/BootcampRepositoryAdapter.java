package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.adapter;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.domain.spi.IBootcampPersistencePort;
import com.pragma.ms_bootcamp.infrastructure.exception.InvalidSortFieldException;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampCapacityEntity;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.mapper.IBootcampEntityMapper;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.projections.BootcampWitchCapacityCountProjection;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampCapacityR2dbcRepository;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository.IBootcampR2dbcRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@Slf4j
public class BootcampRepositoryAdapter implements IBootcampPersistencePort {

    private final IBootcampR2dbcRepository bootcampR2dbcRepository;
    private final IBootcampCapacityR2dbcRepository bootcampCapacityR2dbcRepository;
    private final IBootcampEntityMapper bootcampEntityMapper;

    private final DatabaseClient databaseClient;

    public BootcampRepositoryAdapter(IBootcampR2dbcRepository bootcampR2dbcRepository,
                                     IBootcampCapacityR2dbcRepository bootcampCapacityR2dbcRepository,
                                     IBootcampEntityMapper bootcampEntityMapper, DatabaseClient databaseClient) {
        this.bootcampR2dbcRepository = bootcampR2dbcRepository;
        this.bootcampCapacityR2dbcRepository = bootcampCapacityR2dbcRepository;
        this.bootcampEntityMapper = bootcampEntityMapper;
        this.databaseClient = databaseClient;
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

    @Override
    public Flux<Bootcamp> findAllPaged(int page, int size, String sortBy, String direction) {
        long offset = (long) page * size;
        String orderBy = resolveOrderBy(sortBy, direction);

        String sql = """
                SELECT 
                    b.id,
                    b.name,
                    b.description,
                    b.launch_date,
                    b.duration,
                    COUNT(bc.capacity_id) AS capacity_count
                FROM bootcamps b
                LEFT JOIN bootcamp_capacity bc 
                    ON b.id = bc.bootcamp_id
                GROUP BY b.id, b.name, b.description, b.launch_date, b.duration
                ORDER BY %s
                LIMIT %d OFFSET %d
            """.formatted(orderBy, size, offset);

        return databaseClient.sql(sql)
                .map((row, meta) -> new BootcampWitchCapacityCountProjection(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("description", String.class),
                        row.get("launch_date", LocalDate.class),
                        row.get("duration", Integer.class),
                        row.get("capacity_count", Long.class)
                ))
                .all()
                .map(bootcampEntityMapper::toModelProjection);
    }

    @Override
    public Mono<Long> countAllBootcamps() {
        return bootcampR2dbcRepository.count();
    }

    @Override
    public Flux<Long> getCapacitiesIdsByBootcampId(Long bootcampId) {
        return bootcampCapacityR2dbcRepository.findCapacitiesIdsByBootcampId(bootcampId);
    }

    private String resolveOrderBy(String sortBy, String direction) {
        String column = switch (sortBy.toLowerCase()) {
            case "name" -> "b.name";
            case "capacitycount" -> "capacity_count";
            default -> throw new InvalidSortFieldException("Invalid sort field. Only 'name' and 'capacitycount' are allowed.");
        };
        String dir = direction.equalsIgnoreCase("desc") ? "DESC" : "ASC";
        return column + " " + dir;
    }
}
