CREATE TABLE image_metadata (
    id UUID PRIMARY KEY,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP NOT NULL,
    image_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    file_exists BOOLEAN DEFAULT TRUE,
    camera_id UUID
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(64) NOT NULL,
    last_name VARCHAR(64) NOT NULL,
    email VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_active TIMESTAMP DEFAULT NOW()
);

CREATE TABLE camera (
    id UUID PRIMARY KEY,
    owner_id UUID REFERENCES users(id),
    name VARCHAR(64),
    password VARCHAR(64) NOT NULL,
    type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_active TIMESTAMP
);

CREATE TABLE authentication (
    user_id UUID PRIMARY KEY,
    random_string VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE image_entity (
  id UUID PRIMARY KEY,
  metadata_id UUID REFERENCES image_metadata(id),
  x SMALLINT,
  y SMALLINT,
  width SMALLINT,
  height SMALLINT,
  type VARCHAR(16)
);

CREATE INDEX idx_token ON authentication(random_string);

CREATE TABLE groups (
  id UUID PRIMARY KEY,
  owner_id UUID REFERENCES users(id),
  name VARCHAR(64) NOT NULL
);

CREATE TABLE users_groups (
  id UUID PRIMARY KEY,
  user_id UUID REFERENCES users(id),
  group_id UUID REFERENCES groups(id)
);

CREATE TABLE groups_cameras (
  id UUID PRIMARY KEY,
  group_id UUID REFERENCES groups(id),
  camera_id UUID REFERENCES camera(id)
);
