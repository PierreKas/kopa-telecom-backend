SET search_path TO customer_identity;

-- Same bug class confirmed live 2026-10-28 in kopa.customer-self-care's transactions table:
-- KTRN's own PROD_CD values aren't short codes ("KOPA TelecomMainProduct" is 23 chars) and
-- already exceed VARCHAR(20) - widening here pre-emptively since activation_requests.product_code
-- stores the same KTRN price-plan product code.
ALTER TABLE activation_requests ALTER COLUMN product_code TYPE VARCHAR(100);
