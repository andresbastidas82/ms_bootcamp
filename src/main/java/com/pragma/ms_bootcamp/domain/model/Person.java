package com.pragma.ms_bootcamp.domain.model;

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
public class Person {

    private Long id;
    private String name;
    private String email;
    private String identificationNumber;
}
