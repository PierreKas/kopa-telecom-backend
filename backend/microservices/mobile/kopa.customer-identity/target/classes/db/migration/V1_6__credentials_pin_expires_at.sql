SET search_path TO customer_identity;

-- Admin-activated customers now get a random, system-generated initial PIN (sent via SMS) rather
-- than one typed by the admin (2026-11-03 - admin typing/knowing a customer's PIN was flagged as
-- a confidentiality risk). That PIN is only valid for 15 minutes; NULL means the current pin_hash
-- is a permanent, customer-set PIN with no expiry.
ALTER TABLE credentials ADD COLUMN pin_expires_at TIMESTAMPTZ;
