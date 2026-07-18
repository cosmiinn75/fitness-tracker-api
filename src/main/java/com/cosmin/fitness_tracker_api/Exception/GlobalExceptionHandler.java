package com.cosmin.fitness_tracker_api.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleAccountAlreadyExistsException(AccountAlreadyExistsException e) {

        Map<String, String> response = new HashMap<>();
        response.put("error" , "Bad request");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(ExerciseDefinitionNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleExerciseDefinitionNotFoundException(ExerciseDefinitionNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Not found");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Not found");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentialsException(InvalidCredentialsException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Unauthorized");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Unauthorized");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidBodyException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBodyException(InvalidBodyException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Bad request");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDateRangeException(InvalidDateRangeException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Bad request");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(NameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleNameAlreadyExistsException(NameAlreadyExistsException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Conflict");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotAuthException.class)
    public ResponseEntity<Map<String, String>> handleUserNotAuthException(UserNotAuthException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Unauthorized");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(WorkoutNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWorkoutNotFoundException(WorkoutNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Not found");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PersonalRecordNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePersonalRecordNotFoundException(PersonalRecordNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Not found");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(WorkoutExerciseNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWorkoutExerciseNotFoundException(WorkoutExerciseNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Not found");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExerciseSetNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleExerciseSetNotFoundException(ExerciseSetNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error" , "Not found");
        response.put("message" , e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> validException(MethodArgumentNotValidException exc){
        Map<String,String> response = new HashMap<>();

        for (FieldError fieldError : exc.getBindingResult().getFieldErrors()) {
            response.put(fieldError.getField() , fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
    }
}
