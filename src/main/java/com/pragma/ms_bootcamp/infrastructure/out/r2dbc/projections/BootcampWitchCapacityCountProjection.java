package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.projections;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BootcampWitchCapacityCountProjection {
    private Long id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer duration;
    private Long capacityCount;
}
