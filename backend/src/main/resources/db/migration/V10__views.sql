-- V10: Reporting views (with aggregates and subqueries).

-- Revenue per store over paid/fulfilled orders (subquery filters by order status).
CREATE VIEW v_store_revenue AS
SELECT oi.store_id,
       COUNT(DISTINCT oi.order_id) AS orders_count,
       SUM(oi.quantity)            AS units_sold,
       SUM(oi.line_total)          AS revenue
FROM order_items oi
WHERE oi.order_id IN (SELECT id FROM orders WHERE status IN ('PAID', 'FULFILLED'))
GROUP BY oi.store_id;

-- Aggregated sales per product.
CREATE VIEW v_top_products AS
SELECT oi.product_id,
       oi.store_id,
       MAX(oi.name)       AS name,
       SUM(oi.quantity)   AS units_sold,
       SUM(oi.line_total) AS revenue
FROM order_items oi
GROUP BY oi.product_id, oi.store_id;

-- Per-order summary with a correlated scalar subquery for item count.
CREATE VIEW v_order_summary AS
SELECT o.id           AS order_id,
       o.order_number,
       o.user_id,
       o.status,
       o.total,
       o.created_at,
       (SELECT COUNT(*) FROM order_items oi WHERE oi.order_id = o.id) AS item_count
FROM orders o;
