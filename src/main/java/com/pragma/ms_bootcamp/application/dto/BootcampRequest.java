package com.pragma.ms_bootcamp.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BootcampRequest {

    @NotBlank(message = "El nombre del bootcamp es requerido")
    private String name;

    @NotBlank(message = "La descripción del bootcamp es requerida")
    private String description;

    @NotNull(message = "La fecha de lanzamiento del bootcamp es requerida")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate launchDate;

    @NotNull(message = "La duración del bootcamp es requerida")
    private Integer duration;

    @NotNull(message = "Las capacidades del bootcamp son requeridas")
    private List<Long> capacityIds;
}
