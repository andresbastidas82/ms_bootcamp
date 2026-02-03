package com.pragma.ms_bootcamp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationRequest {
    private Long bootcampId;
    private Long personId;
}
