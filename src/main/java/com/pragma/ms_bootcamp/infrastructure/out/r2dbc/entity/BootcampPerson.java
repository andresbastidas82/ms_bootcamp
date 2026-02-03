package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("bootcamp_person")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BootcampPerson {
    private Long bootcampId;
    private Long personId;

    //Fecha de inscripcion
    private LocalDateTime enrolledAt;

}
