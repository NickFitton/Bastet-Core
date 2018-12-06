CREATE TABLE camera (
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_upload TIMESTAMP
);

CREATE TABLE key (
    id UUID PRIMARY KEY,
    camera_id UUID NOT NULL,
    key_type VARCHAR(10) NOT NULL,
    private_key VARCHAR(858),
    public_key VARCHAR(1115)
);

CREATE INDEX idx_key_type ON key (key_type);