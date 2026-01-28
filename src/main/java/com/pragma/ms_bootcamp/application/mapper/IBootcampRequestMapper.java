package com.pragma.ms_bootcamp.application.mapper;

import com.pragma.ms_bootcamp.application.dto.BootcampRequest;
import com.pragma.ms_bootcamp.application.dto.BootcampResponse;
import com.pragma.ms_bootcamp.domain.model.Bootcamp;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface IBootcampRequestMapper {

    BootcampResponse toBootcampResponse(Bootcamp bootcamp);

    Bootcamp toBootcampModel(BootcampRequest bootcampRequest);
}
