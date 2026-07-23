CREATE TABLE training_goals (
                                id BIGINT NOT NULL AUTO_INCREMENT,
                                user_id BIGINT NOT NULL,
                                exercise_definition_id BIGINT NOT NULL,
                                target_weight DOUBLE NOT NULL,
                                target_reps INT NOT NULL,
                                target_date DATE NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

                                PRIMARY KEY (id),

                                CONSTRAINT chk_training_goals_target_weight
                                    CHECK (target_weight > 0),

                                CONSTRAINT chk_training_goals_target_reps
                                    CHECK (target_reps > 0),

                                INDEX idx_training_goals_user_exercise_status (
                                                                               user_id,
                                                                               exercise_definition_id,
                                                                               status
                                    ),

                                CONSTRAINT fk_training_goals_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE,

                                CONSTRAINT fk_training_goals_exercise_definition
                                    FOREIGN KEY (exercise_definition_id)
                                        REFERENCES exercise_definitions(id),

                                INDEX idx_training_goals_exercise_definition (
        exercise_definition_id
    )
);