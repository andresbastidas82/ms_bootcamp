package com.pragma.ms_bootcamp.infrastructure.input.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportResponse <T>{
    private String message;
    private Boolean isSuccess;
    private T data;
}
