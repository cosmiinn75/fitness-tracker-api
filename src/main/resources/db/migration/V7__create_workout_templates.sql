CREATE TABLE workout_templates
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    template_name   VARCHAR(50) NOT NULL,
    normalized_name VARCHAR(50) NOT NULL,
    created_at      DATE        NOT NULL,
    user_id         BIGINT      NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_workout_templates_user_name
        UNIQUE (user_id, normalized_name),

    CONSTRAINT fk_workout_templates_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);


CREATE TABLE workout_template_exercises
(
    id                     BIGINT NOT NULL AUTO_INCREMENT,
    exercise_number        INT    NOT NULL,
    workout_template_id    BIGINT NOT NULL,
    exercise_definition_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_template_exercises_number
        UNIQUE (workout_template_id, exercise_number),

    CONSTRAINT fk_template_exercises_template
        FOREIGN KEY (workout_template_id)
            REFERENCES workout_templates (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_template_exercises_definition
        FOREIGN KEY (exercise_definition_id)
            REFERENCES exercise_definitions (id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_template_exercise_number
        CHECK (exercise_number > 0)
);


CREATE TABLE workout_template_sets
(
    id                           BIGINT    NOT NULL AUTO_INCREMENT,
    set_number                   INT       NOT NULL,
    target_weight                FLOAT(53) NULL,
    target_reps                  INT       NOT NULL,
    target_rir                   INT       NULL,
    workout_template_exercise_id BIGINT    NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_template_sets_number
        UNIQUE (workout_template_exercise_id, set_number),

    CONSTRAINT fk_template_sets_exercise
        FOREIGN KEY (workout_template_exercise_id)
            REFERENCES workout_template_exercises (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_template_set_number
        CHECK (set_number > 0),

    CONSTRAINT chk_template_target_weight
        CHECK (target_weight IS NULL OR target_weight > 0),

    CONSTRAINT chk_template_target_reps
        CHECK (target_reps > 0),

    CONSTRAINT chk_template_target_rir
        CHECK (target_rir IS NULL OR target_rir BETWEEN 0 AND 10)
);