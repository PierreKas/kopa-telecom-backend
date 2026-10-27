SET search_path TO customer_identity;

CREATE TABLE customers (
    id               VARCHAR(64) PRIMARY KEY,
    msisdn           VARCHAR(15) NOT NULL UNIQUE,
    national_id      VARCHAR(20),
    passport_number  VARCHAR(20),
    iccid            VARCHAR(19),
    cust_id          VARCHAR(10),
    cust_type        VARCHAR(1),
    full_name        VARCHAR(255) NOT NULL,
    birth_day        DATE,
    email            VARCHAR(255),
    address_detail   VARCHAR(300),
    so_id            VARCHAR(3),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING_ACTIVATION',
    created_at       TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL
);

CREATE TABLE credentials (
    customer_id      VARCHAR(64) PRIMARY KEY REFERENCES customers(id),
    pin_hash         VARCHAR(255) NOT NULL,
    failed_attempts  INT NOT NULL DEFAULT 0,
    locked_until     TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ NOT NULL
);

CREATE TABLE auth_tokens (
    token            VARCHAR(64) PRIMARY KEY,
    customer_id      VARCHAR(64) NOT NULL REFERENCES customers(id),
    issued_at        TIMESTAMPTZ NOT NULL,
    expires_at       TIMESTAMPTZ NOT NULL,
    revoked          BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_auth_tokens_customer_id ON auth_tokens (customer_id);
