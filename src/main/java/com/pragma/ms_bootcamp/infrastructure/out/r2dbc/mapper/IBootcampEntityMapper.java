package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.mapper;

import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IBootcampEntityMapper {

    Bootcamp toBootcampModel(BootcampEntity bootcampEntity);

    BootcampEntity toBootcampEntity(Bootcamp bootcamp);
}
