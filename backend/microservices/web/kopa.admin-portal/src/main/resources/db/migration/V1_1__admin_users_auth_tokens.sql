SET search_path TO admin_portal;

-- Own, independent credential space - never resolves a Mobile bearer token and vice versa.
-- Single role for now (FR-13's "role-based access" with exactly one role, Admin, until real
-- usage patterns justify splitting it) - the role column exists so a second role is a data
-- migration later, not a schema one.
CREATE TABLE admin_users (
    id              VARCHAR(64) PRIMARY KEY,
    username        VARCHAR(50) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE TABLE admin_auth_tokens (
    token       VARCHAR(64) PRIMARY KEY,
    admin_id    VARCHAR(64) NOT NULL,
    issued_at   TIMESTAMPTZ NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_admin_auth_tokens_admin_id ON admin_auth_tokens (admin_id);
