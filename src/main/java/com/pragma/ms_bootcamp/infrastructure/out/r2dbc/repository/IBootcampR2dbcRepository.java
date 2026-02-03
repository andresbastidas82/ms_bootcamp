package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.repository;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

public interface IBootcampR2dbcRepository extends ReactiveCrudRepository<BootcampEntity, Long> {

    @Query("""
        SELECT b.*
        FROM bootcamp_db.bootcamps b
        JOIN bootcamp_db.bootcamp_person bp ON b.id = bp.bootcamp_id
        WHERE (b.launch_date >= :date OR DATE_ADD(b.launch_date, INTERVAL b.duration DAY) >= :date)
        AND bp.person_id = :personId
        """)
    Flux<Bootcamp> findActiveBootcampsByPersonId(@Param("date") LocalDate date, @Param("personId") Long personId);
}
