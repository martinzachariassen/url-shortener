CREATE TABLE IF NOT EXISTS url_mapping (
    id SERIAL PRIMARY KEY,
    short_url VARCHAR(255) NOT NULL,
    original_url VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);
