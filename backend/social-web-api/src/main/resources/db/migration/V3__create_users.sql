-- Create users table for authentication/registration
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

-- Enforce unique emails
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (email);