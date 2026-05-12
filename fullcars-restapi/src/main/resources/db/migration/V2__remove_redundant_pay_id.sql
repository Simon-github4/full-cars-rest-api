-- =============================================================================
-- MIGRATION: Remove redundant pay_id from pay_allocation
-- Description: pay_id was redundant since PaymentSplit already links to Pay
-- =============================================================================

-- Drop redundant column and index
ALTER TABLE pay_allocation DROP COLUMN IF EXISTS pay_id;
DROP INDEX IF EXISTS idx_pay_allocation_pay_id;
