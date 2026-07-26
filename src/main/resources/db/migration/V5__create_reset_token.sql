CREATE TABLE reset_token (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             reset_token VARCHAR(255) NOT NULL,
                             expires_at DATETIME(6) NOT NULL,
                             user_id BIGINT NOT NULL,

                             PRIMARY KEY (id),
                             CONSTRAINT uk_reset_token UNIQUE (reset_token),
                             CONSTRAINT fk_reset_token_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE
);

CREATE INDEX idx_reset_token_user_id
    ON reset_token(user_id);

