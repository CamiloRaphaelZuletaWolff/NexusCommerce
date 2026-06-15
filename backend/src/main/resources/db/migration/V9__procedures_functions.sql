-- V9: Stored procedure + reporting function.

-- Stored procedure: atomically reserve stock for a product within the caller's
-- transaction. Locks the row (FOR UPDATE) and raises if stock is missing or
-- insufficient, which rolls back the surrounding ACID checkout.
CREATE PROCEDURE sp_reserve_inventory(p_product_id UUID, p_qty INT)
LANGUAGE plpgsql
AS $$
DECLARE
    v_available INT;
BEGIN
    SELECT available_quantity INTO v_available
    FROM inventory
    WHERE product_id = p_product_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'No inventory record for product %', p_product_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_available < p_qty THEN
        RAISE EXCEPTION 'Insufficient stock for product %: have %, need %', p_product_id, v_available, p_qty
            USING ERRCODE = 'check_violation';
    END IF;

    UPDATE inventory
    SET available_quantity = available_quantity - p_qty,
        reserved_quantity  = reserved_quantity + p_qty,
        updated_at         = now()
    WHERE product_id = p_product_id;
END;
$$;

-- Reporting function (uses a subquery): sales summary for a store over paid orders.
CREATE FUNCTION fn_store_sales_summary(p_store_id UUID)
RETURNS TABLE (orders_count BIGINT, units_sold BIGINT, revenue NUMERIC)
LANGUAGE sql
AS $$
    SELECT COUNT(DISTINCT oi.order_id),
           COALESCE(SUM(oi.quantity), 0),
           COALESCE(SUM(oi.line_total), 0)
    FROM order_items oi
    WHERE oi.store_id = p_store_id
      AND oi.order_id IN (SELECT id FROM orders WHERE status IN ('PAID', 'FULFILLED'));
$$;
