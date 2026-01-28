package com.pragma.ms_bootcamp.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bootcamp {
    private Long id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer duration;
    private List<Long> capacityIds;
}