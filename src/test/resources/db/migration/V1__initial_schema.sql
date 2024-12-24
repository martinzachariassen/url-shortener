CREATE TABLE IF NOT EXISTS url_mapping (
    id SERIAL PRIMARY KEY,
    short_url VARCHAR(255) NOT NULL UNIQUE,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_short_url_unique ON url_mapping(short_url);
