package com.cosmin.fitness_tracker_api.Exception;

public class PersonalRecordNotFoundException extends RuntimeException {
    public PersonalRecordNotFoundException(String message) {
        super(message);
    }
}
