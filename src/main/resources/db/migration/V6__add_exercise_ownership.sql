ALTER TABLE exercise_definitions
    ADD COLUMN normalized_name VARCHAR(255) NULL AFTER name,
    ADD COLUMN exercise_type VARCHAR(255) NULL AFTER normalized_name,
    ADD COLUMN owner_id BIGINT NULL AFTER exercise_type,
    ADD COLUMN archived BOOLEAN NOT NULL DEFAULT FALSE AFTER owner_id;



UPDATE exercise_definitions
SET normalized_name = LOWER(
        REGEXP_REPLACE(TRIM(name), '[[:space:]]+', ' ')
                      ),
    exercise_type = 'SYSTEM',
    owner_id = NULL,
    archived = FALSE;



ALTER TABLE exercise_definitions
    MODIFY COLUMN name VARCHAR(255) NOT NULL,
    MODIFY COLUMN normalized_name VARCHAR(255) NOT NULL,
    MODIFY COLUMN exercise_type VARCHAR(255) NOT NULL;



DROP INDEX uk_exercise_definitions_name
    ON exercise_definitions;


ALTER TABLE exercise_definitions
    ADD CONSTRAINT fk_exercise_definitions_owner
        FOREIGN KEY (owner_id)
            REFERENCES users(id),

    ADD CONSTRAINT chk_exercise_definitions_type_owner
        CHECK (
            (exercise_type = 'SYSTEM' AND owner_id IS NULL)
            OR
            (exercise_type = 'CUSTOM' AND owner_id IS NOT NULL)
        );



CREATE UNIQUE INDEX uk_exercise_definitions_owner_normalized_name
    ON exercise_definitions(owner_id, normalized_name);



CREATE INDEX idx_exercise_definitions_type_archived
    ON exercise_definitions(exercise_type, archived);

CREATE INDEX idx_exercise_definitions_owner_archived
    ON exercise_definitions(owner_id, archived);