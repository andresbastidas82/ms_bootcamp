package com.pragma.ms_bootcamp.infrastructure.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CapacityResponse {

    private Long id;
    private String name;
    private String description;

    private List<TechnologyResponse> technologies;
}
