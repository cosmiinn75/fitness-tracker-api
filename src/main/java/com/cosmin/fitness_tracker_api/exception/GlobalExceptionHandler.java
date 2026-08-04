package com.cosmin.fitness_tracker_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ProblemDetail handleAccountAlreadyExistsException(
            AccountAlreadyExistsException exception
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Account already exists",
                exception.getMessage(),
                "ACCOUNT_ALREADY_EXISTS",
                "account-already-exists"
        );
    }

    @ExceptionHandler(ExerciseDefinitionNotFoundException.class)
    public ProblemDetail handleExerciseDefinitionNotFoundException(
            ExerciseDefinitionNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Exercise definition not found",
                exception.getMessage(),
                "EXERCISE_DEFINITION_NOT_FOUND",
                "exercise-definition-not-found"
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFoundException(
            UserNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "User not found",
                exception.getMessage(),
                "USER_NOT_FOUND",
                "user-not-found"
        );
    }

    @ExceptionHandler(TrainingGoalNotFoundException.class)
    public ProblemDetail handleTrainingGoalNotFoundException(
            TrainingGoalNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Training goal not found",
                exception.getMessage(),
                "TRAINING_GOAL_NOT_FOUND",
                "training-goal-not-found"
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentialsException(
            InvalidCredentialsException exception
    ) {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials",
                exception.getMessage(),
                "INVALID_CREDENTIALS",
                "invalid-credentials"
        );
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshTokenException(
            InvalidRefreshTokenException exception
    ) {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid refresh token",
                exception.getMessage(),
                "INVALID_REFRESH_TOKEN",
                "invalid-refresh-token"
        );
    }

    @ExceptionHandler(InvalidBodyException.class)
    public ProblemDetail handleInvalidBodyException(
            InvalidBodyException exception
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                exception.getMessage(),
                "INVALID_REQUEST_BODY",
                "invalid-request-body"
        );
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ProblemDetail handleInvalidDateRangeException(
            InvalidDateRangeException exception
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Invalid date range",
                exception.getMessage(),
                "INVALID_DATE_RANGE",
                "invalid-date-range"
        );
    }

    @ExceptionHandler(DuplicateExerciseDefinitionException.class)
    public ProblemDetail handleDuplicateExerciseDefinitionException(
            DuplicateExerciseDefinitionException exception
    ) {
        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Duplicate exercise definition",
                exception.getMessage(),
                "DUPLICATE_EXERCISE_DEFINITION",
                "duplicate-exercise-definition"
        );
    }

    @ExceptionHandler(InvalidTrainingGoalStatusException.class)
    public ProblemDetail handleInvalidTrainingGoalStatusException(
            InvalidTrainingGoalStatusException exception
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Invalid training goal status",
                exception.getMessage(),
                "INVALID_TRAINING_GOAL_STATUS",
                "invalid-training-goal-status"
        );
    }

    @ExceptionHandler(NameAlreadyExistsException.class)
    public ProblemDetail handleNameAlreadyExistsException(
            NameAlreadyExistsException exception
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Name already exists",
                exception.getMessage(),
                "NAME_ALREADY_EXISTS",
                "name-already-exists"
        );
    }

    @ExceptionHandler(ActiveTrainingGoalAlreadyExistsException.class)
    public ProblemDetail handleActiveTrainingGoalAlreadyExistsException(
            ActiveTrainingGoalAlreadyExistsException exception
    ) {
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Active training goal already exists",
                exception.getMessage(),
                "ACTIVE_TRAINING_GOAL_ALREADY_EXISTS",
                "active-training-goal-already-exists"
        );
    }

    @ExceptionHandler(UserNotAuthException.class)
    public ProblemDetail handleUserNotAuthException(
            UserNotAuthException exception
    ) {
        return createProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication required",
                exception.getMessage(),
                "USER_NOT_AUTHENTICATED",
                "user-not-authenticated"
        );
    }

    @ExceptionHandler(WorkoutNotFoundException.class)
    public ProblemDetail handleWorkoutNotFoundException(
            WorkoutNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Workout not found",
                exception.getMessage(),
                "WORKOUT_NOT_FOUND",
                "workout-not-found"
        );
    }

    @ExceptionHandler(WorkoutTemplateNotFoundException.class)
    public ProblemDetail handleWorkoutTemplateNotFoundException(
            WorkoutTemplateNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Workout template not found",
                exception.getMessage(),
                "WORKOUT_TEMPLATE_NOT_FOUND",
                "workout-template-not-found"
        );
    }

    @ExceptionHandler(PersonalRecordNotFoundException.class)
    public ProblemDetail handlePersonalRecordNotFoundException(
            PersonalRecordNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Personal record not found",
                exception.getMessage(),
                "PERSONAL_RECORD_NOT_FOUND",
                "personal-record-not-found"
        );
    }

    @ExceptionHandler(WorkoutExerciseNotFoundException.class)
    public ProblemDetail handleWorkoutExerciseNotFoundException(
            WorkoutExerciseNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Workout exercise not found",
                exception.getMessage(),
                "WORKOUT_EXERCISE_NOT_FOUND",
                "workout-exercise-not-found"
        );
    }

    @ExceptionHandler(ExerciseSetNotFoundException.class)
    public ProblemDetail handleExerciseSetNotFoundException(
            ExerciseSetNotFoundException exception
    ) {
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Exercise set not found",
                exception.getMessage(),
                "EXERCISE_SET_NOT_FOUND",
                "exercise-set-not-found"
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() != null
                            ? fieldError.getDefaultMessage()
                            : "Invalid value"
            );
        }

        ProblemDetail problemDetail = createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "One or more request fields are invalid",
                "VALIDATION_FAILED",
                "validation-failed"
        );

        problemDetail.setProperty(
                "fieldErrors",
                fieldErrors
        );

        return problemDetail;
    }

    private ProblemDetail createProblemDetail(
            HttpStatus status,
            String title,
            String detail,
            String code,
            String type
    ) {
        ProblemDetail problemDetail =
                ProblemDetail.forStatusAndDetail(
                        status,
                        detail
                );

        problemDetail.setTitle(title);

        problemDetail.setType(
                URI.create("urn:problem:" + type)
        );

        problemDetail.setProperty(
                "code",
                code
        );

        return problemDetail;
    }
}