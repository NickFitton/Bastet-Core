CREATE TABLE camera (
    id UUID PRIMARY KEY,
    password VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_upload TIMESTAMP
);

CREATE TABLE authentication (
    user_id UUID PRIMARY KEY,
    random_string VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_token ON authentication(random_string);

ALTER TABLE image_metadata ADD camera_id UUID;