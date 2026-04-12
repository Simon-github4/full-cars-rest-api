-- =============================================================================
-- MIGRATION: Add Multi-Payment Support
-- Description: Adds customer credits and payment allocations for multi-sale payments
-- =============================================================================

-- 1. Add credit_balance column to customer table
ALTER TABLE customer ADD COLUMN IF NOT EXISTS credit_balance DECIMAL(19, 2) DEFAULT 0.00;

-- 2. Add credit columns to pay table
ALTER TABLE pay ADD COLUMN IF NOT EXISTS credit_used DECIMAL(19, 2) DEFAULT 0.00;
ALTER TABLE pay ADD COLUMN IF NOT EXISTS credit_generated DECIMAL(19, 2) DEFAULT 0.00;

-- 3. Create customer_credits table
CREATE TABLE IF NOT EXISTS customer_credit (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES customer(id),
    amount DECIMAL(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(500),
    pay_id BIGINT
);

-- 4. Create pay_allocation table with is_credit flag
CREATE TABLE IF NOT EXISTS pay_allocation (
    id BIGSERIAL PRIMARY KEY,
    pay_id BIGINT NOT NULL REFERENCES pay(id),
    sale_id BIGINT NOT NULL REFERENCES sale(sale_id),
    amount_applied DECIMAL(19, 2) NOT NULL,
    is_credit BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_customer_credit_customer_id ON customer_credit(customer_id);
CREATE INDEX IF NOT EXISTS idx_pay_allocation_pay_id ON pay_allocation(pay_id);
CREATE INDEX IF NOT EXISTS idx_pay_allocation_sale_id ON pay_allocation(sale_id);
CREATE INDEX IF NOT EXISTS idx_pay_allocation_is_credit ON pay_allocation(is_credit);

-- =============================================================================
-- MIGRATION: Migrate existing payments to PayAllocation
-- Description: Creates PayAllocation records for all existing payments that have a sale_id
-- All migrated payments are marked as is_credit = false (cash payments)
-- =============================================================================

-- 6. Migrate existing payments to PayAllocation
INSERT INTO pay_allocation (pay_id, sale_id, amount_applied, is_credit)
SELECT p.id, p.sale_id, p.amount, FALSE
FROM pay p
WHERE p.sale_id IS NOT NULL
AND p.id NOT IN (SELECT pay_id FROM pay_allocation);

-- 7. Initialize credit_balance for customers (default 0)
UPDATE customer
SET credit_balance = 0.00
WHERE credit_balance IS NULL;
