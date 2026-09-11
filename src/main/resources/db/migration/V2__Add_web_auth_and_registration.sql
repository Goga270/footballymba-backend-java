-- 1. Добавляем поля веб-авторизации в существующую таблицу игроков
ALTER TABLE players 
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- 2. Создаем таблицу для одноразовых "волшебных токенов" входа
CREATE TABLE IF NOT EXISTS magic_tokens (
    id SERIAL PRIMARY KEY,
    player_id INTEGER NOT NULL UNIQUE REFERENCES players(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL
);

-- Создаем индекс для быстрого поиска токенов входа
CREATE INDEX IF NOT EXISTS idx_magic_tokens_token ON magic_tokens(token);