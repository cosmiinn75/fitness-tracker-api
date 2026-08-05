package com.cosmin.fitness_tracker_api.exception;

public class PersonalRecordNotFoundException extends RuntimeException {
    public PersonalRecordNotFoundException(String message) {
        super(message);
    }
}
