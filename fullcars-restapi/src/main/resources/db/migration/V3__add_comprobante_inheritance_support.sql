-- =============================================================================
-- MIGRATION: Add single-table comprobante inheritance
-- Description: Renames factura to comprobante and supports Factura/CreditNote
--              rows in the same table.
-- =============================================================================

ALTER TABLE IF EXISTS factura RENAME TO comprobante;

ALTER TABLE comprobante ADD COLUMN IF NOT EXISTS comprobante_type VARCHAR(20) NOT NULL DEFAULT 'FACTURA';
ALTER TABLE comprobante ADD COLUMN IF NOT EXISTS comprobante_asociado_id BIGINT;
ALTER TABLE comprobante ADD COLUMN IF NOT EXISTS customer_credit_id BIGINT;

ALTER TABLE comprobante DROP COLUMN IF EXISTS sale_sale_id;
DO $$
DECLARE
    constraint_name_to_drop TEXT;
BEGIN
    FOR constraint_name_to_drop IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
            ON tc.constraint_name = kcu.constraint_name
            AND tc.table_schema = kcu.table_schema
        WHERE tc.table_name = 'comprobante'
            AND tc.constraint_type = 'UNIQUE'
            AND kcu.column_name IN ('sale_sale_id', 'sale_id')
    LOOP
        EXECUTE format('ALTER TABLE comprobante DROP CONSTRAINT IF EXISTS %I', constraint_name_to_drop);
    END LOOP;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_comprobante_asociado'
            AND table_name = 'comprobante'
    ) THEN
        ALTER TABLE comprobante
            ADD CONSTRAINT fk_comprobante_asociado
            FOREIGN KEY (comprobante_asociado_id) REFERENCES comprobante(id);
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_comprobante_customer_credit'
            AND table_name = 'comprobante'
    ) THEN
        ALTER TABLE comprobante
            ADD CONSTRAINT fk_comprobante_customer_credit
            FOREIGN KEY (customer_credit_id) REFERENCES customer_credit(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_comprobante_type ON comprobante(comprobante_type);
CREATE INDEX IF NOT EXISTS idx_comprobante_asociado ON comprobante(comprobante_asociado_id);
