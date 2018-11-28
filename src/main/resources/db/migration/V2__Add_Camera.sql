CREATE TABLE camera (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_upload TIMESTAMP
);
