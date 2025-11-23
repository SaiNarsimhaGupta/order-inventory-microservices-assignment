
INSERT INTO products (id, product_code, name, description, category, minimum_stock, handler_type, created_at, updated_at)
VALUES
    (1, 'PROD-001', 'Aspirin 500mg', 'Pain reliever and fever reducer', 'HEALTH_AND_BEAUTY', 100, 'STANDARD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'PROD-002', 'Face Masks N95', 'Protective face masks', 'HEALTH_AND_BEAUTY', 20, 'FIFO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'PROD-003', 'Laptop Dell XPS 13', 'High-performance laptop computer', 'ELECTRONICS', 5, 'STANDARD', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inventory batches for the retained products
INSERT INTO inventory_batches (id, product_id, batch_number, quantity, expiry_date, manufacturing_date, supplier_name, cost_per_unit, status, created_at, updated_at)
VALUES
    -- Aspirin: two active batches + one expired (edge case)
    (1, 1, 'ASP-BATCH-001', 200, '2025-12-01', '2024-06-01', 'PharmaCorp Inc', 2.50, 'ACTIVE', '2024-06-01 10:00:00', CURRENT_TIMESTAMP),
    (2, 1, 'ASP-BATCH-002', 150, '2026-01-15', '2024-07-15', 'PharmaCorp Inc', 2.50, 'ACTIVE', '2024-07-15 10:00:00', CURRENT_TIMESTAMP),
    (23, 1, 'ASP-BATCH-EXPIRED', 50, '2024-05-01', '2023-05-01', 'OldStock Inc', 2.30, 'ACTIVE', '2023-05-01 10:00:00', CURRENT_TIMESTAMP),

    -- Face Masks: one active batch
    (10, 2, 'MASK-BATCH-001', 50, '2028-01-01', '2024-01-01', 'SafetyFirst Ltd', 15.00, 'ACTIVE', '2024-01-01 10:00:00', CURRENT_TIMESTAMP),

    -- Laptop: single batch (non-perishable, expiry_date NULL)
    (13, 3, 'LAPTOP-BATCH-001', 10, NULL, '2025-01-15', 'Dell Technologies', 1200.00, 'ACTIVE', '2025-01-15 10:00:00', CURRENT_TIMESTAMP),

    -- Inactive batch example (should be ignored by availability checks)
    (24, 1, 'ASP-BATCH-INACTIVE', 100, '2026-12-31', '2024-10-01', 'TestSupplier Ltd', 3.20, 'INACTIVE', '2024-10-01 10:00:00', CURRENT_TIMESTAMP);
