package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity;

import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@Table("bootcamp_capacity")
@NoArgsConstructor
public class BootcampCapacityEntity {

    private Long bootcampId;
    private Long capacityId;

    public BootcampCapacityEntity(Long bootcampId, Long capacityId) {
        this.bootcampId = bootcampId;
        this.capacityId = capacityId;
    }
}
