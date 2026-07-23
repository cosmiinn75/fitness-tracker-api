-- Authentication and user validation
CREATE UNIQUE INDEX uk_users_username
    ON users(username);

CREATE UNIQUE INDEX uk_users_email
    ON users(email);


-- Exercise definition lookup and duplicate-name protection
CREATE UNIQUE INDEX uk_exercise_definitions_name
    ON exercise_definitions(name);


-- Workout listing, date filtering and date ordering
CREATE INDEX idx_workouts_user_date
    ON workouts(user_id, date);


-- Retrieve the exercises of a workout in their correct order
CREATE INDEX idx_workout_exercises_workout_number
    ON workout_exercises(workout_id, exercise_number);


-- Exercise history and personal records
CREATE INDEX idx_workout_exercises_definition_workout
    ON workout_exercises(exercise_definition_id, workout_id);


-- Retrieve the sets of an exercise in their correct order
CREATE INDEX idx_exercise_sets_workout_exercise_number
    ON exercise_sets(workout_exercise_id, set_number);