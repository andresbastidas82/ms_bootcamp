package com.pragma.ms_bootcamp.infrastructure.exception;

public class InvalidSortFieldException extends RuntimeException {
    public InvalidSortFieldException(String message) {
        super(message);
    }
}
