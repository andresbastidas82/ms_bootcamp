package com.pragma.ms_bootcamp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistrationResponse {
    private Boolean stateRegistration;
    private String message;
    private Long bootcampId;
    private Long personId;
}
