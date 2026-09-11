CREATE TABLE refresh_tokens (
    id SERIAL PRIMARY KEY,
    player_id INTEGER NOT NULL REFERENCES players(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL
);

-- Индекс для мгновенного поиска и валидации токенов обновления
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);