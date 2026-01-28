package com.pragma.ms_bootcamp.infrastructure.out.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.List;

@Table("bootcamps")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BootcampEntity {

    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer duration;

    @Transient
    private List<Long> capacityIds;
}
