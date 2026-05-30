-- =====================================================
-- Order Manager System - Seed Data Script
-- Run this AFTER init.sql to populate test data
-- =====================================================

-- 1. Insert User Accounts
-- Note: Credentials must match AuthServiceImpl hardcoded users
INSERT INTO sys_user_role (user_id, role_name, role_desc, status) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin', 'System Administrator', 'normal'),
    ('a0000000-0000-0000-0000-000000000002', 'operator', 'Normal Operator', 'normal'),
    ('a0000000-0000-0000-0000-000000000003', 'viewer', 'Read-Only Viewer', 'normal')
ON CONFLICT DO NOTHING;

-- 2. Insert Customers
INSERT INTO customers (customer_id, customer_name, contact_person, contact_phone, status) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Huawei Technologies Co.', 'Zhang San', '13800138001', 'normal'),
    ('c0000000-0000-0000-0000-000000000002', 'Xiaomi Technology', 'Li Si', '13800138002', 'normal'),
    ('c0000000-0000-0000-0000-000000000003', 'Alibaba Group', 'Wang Wu', '13800138003', 'normal'),
    ('c0000000-0000-0000-0000-000000000004', 'Tencent Computer System', 'Zhao Liu', '13800138004', 'normal'),
    ('c0000000-0000-0000-0000-000000000005', 'ByteDance Technology', 'Sun Qi', '13800138005', 'normal'),
    ('c0000000-0000-0000-0000-000000000006', 'JD.com', 'Zhou Ba', '13800138006', 'normal'),
    ('c0000000-0000-0000-0000-000000000007', 'NetEase Inc.', 'Wu Jiu', '13800138007', 'normal'),
    ('c0000000-0000-0000-0000-000000000008', 'Meituan', 'Zheng Shi', '13800138008', 'normal')
ON CONFLICT (customer_name) DO NOTHING;

-- 3. Insert Equipment
INSERT INTO equipment (equipment_id, equipment_code, equipment_name, specification, status) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'EQ-001', 'Server Rack', '42U Standard Rack, Dual Power Supply', 'normal'),
    ('e0000000-0000-0000-0000-000000000002', 'EQ-002', 'Core Switch', '48-Port 10G Managed Switch', 'normal'),
    ('e0000000-0000-0000-0000-000000000003', 'EQ-003', 'Firewall', 'Next-Gen Firewall 10Gbps', 'normal'),
    ('e0000000-0000-0000-0000-000000000004', 'EQ-004', 'Storage Array', 'All-Flash Storage 100TB', 'normal'),
    ('e0000000-0000-0000-0000-000000000005', 'EQ-005', 'Precision Air Conditioner', '20KW Precision AC Unit', 'normal'),
    ('e0000000-0000-0000-0000-000000000006', 'EQ-006', 'UPS Power System', 'Online UPS 20KVA', 'repair'),
    ('e0000000-0000-0000-0000-000000000007', 'EQ-007', 'Blade Server', '256-Core Blade Server', 'normal'),
    ('e0000000-0000-0000-0000-000000000008', 'EQ-008', 'Load Balancer', 'ADX 4-Layer and 7-Layer LB', 'normal'),
    ('e0000000-0000-0000-0000-000000000009', 'EQ-009', 'Fiber Channel Switch', '16-Port Fiber Switch', 'normal'),
    ('e0000000-0000-0000-0000-000000000010', 'EQ-010', 'Monitor Camera', '4K HD Network Camera', 'disabled')
ON CONFLICT (equipment_code) DO NOTHING;

-- 4. Insert Sample Orders (20 orders, using admin user_id)
-- Order statuses: pending, approved, shipped, completed
INSERT INTO orders (order_id, customer_id, equipment_id, order_type, order_amount, delivery_date, order_status, remarks, created_by, deleted) VALUES
    ('o0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'sales', 85000.00, CURRENT_DATE + INTERVAL '5 days', 'approved', 'Urgent deployment, prioritize', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', 'sales', 42000.00, CURRENT_DATE + INTERVAL '7 days', 'approved', 'Network upgrade project', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', 'purchase', 128000.00, CURRENT_DATE + INTERVAL '10 days', 'pending', 'Security infrastructure refresh', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', 'sales', 350000.00, CURRENT_DATE + INTERVAL '14 days', 'approved', 'Large storage expansion', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000005', 'purchase', 65000.00, CURRENT_DATE + INTERVAL '3 days', 'shipped', 'DC cooling system upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000006', 'c0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000007', 'sales', 520000.00, CURRENT_DATE + INTERVAL '20 days', 'pending', 'High-density compute cluster', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000007', 'c0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000008', 'sales', 88000.00, CURRENT_DATE + INTERVAL '8 days', 'completed', 'Load balancing for new app', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000008', 'c0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000001', 'purchase', 72000.00, CURRENT_DATE + INTERVAL '12 days', 'approved', 'Rack expansion for new region', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000009', 'c0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000009', 'sales', 33000.00, CURRENT_DATE + INTERVAL '6 days', 'pending', 'SAN network upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000010', 'c0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000003', 'purchase', 198000.00, CURRENT_DATE + INTERVAL '15 days', 'approved', 'Firewall cluster replacement', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000011', 'c0000000-0000-0000-0000-000000000006', 'e0000000-0000-0000-0000-000000000002', 'sales', 55000.00, CURRENT_DATE + INTERVAL '9 days', 'shipped', 'Core network refresh', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000012', 'c0000000-0000-0000-0000-000000000007', 'e0000000-0000-0000-0000-000000000004', 'sales', 280000.00, CURRENT_DATE + INTERVAL '18 days', 'pending', 'Game server storage upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000013', 'c0000000-0000-0000-0000-000000000008', 'e0000000-0000-0000-0000-000000000005', 'purchase', 48000.00, CURRENT_DATE + INTERVAL '4 days', 'completed', 'DC temperature control', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000014', 'c0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000008', 'sales', 120000.00, CURRENT_DATE + INTERVAL '11 days', 'approved', 'Traffic load balancer setup', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000015', 'c0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000007', 'sales', 680000.00, CURRENT_DATE + INTERVAL '25 days', 'pending', 'AI training cluster', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000016', 'c0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000009', 'purchase', 29000.00, CURRENT_DATE + INTERVAL '2 days', 'shipped', 'SAN fabric expansion', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000017', 'c0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'sales', 95000.00, CURRENT_DATE + INTERVAL '13 days', 'completed', 'Cloud region rack build-out', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000018', 'c0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000002', 'purchase', 38000.00, CURRENT_DATE + INTERVAL '1 day', 'approved', 'Switch stack replacement', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000019', 'c0000000-0000-0000-0000-000000000006', 'e0000000-0000-0000-0000-000000000004', 'sales', 410000.00, CURRENT_DATE + INTERVAL '22 days', 'pending', 'Logistics platform storage', 'a0000000-0000-0000-0000-000000000001', false),
    ('o0000000-0000-0000-0000-000000000020', 'c0000000-0000-0000-0000-000000000007', 'e0000000-0000-0000-0000-000000000008', 'sales', 105000.00, CURRENT_DATE + INTERVAL '7 days', 'approved', 'Gaming platform load balance', 'a0000000-0000-0000-0000-000000000001', false)
ON CONFLICT DO NOTHING;

-- 5. Verify data counts
SELECT 'sys_user_role' AS table_name, COUNT(*) AS row_count FROM sys_user_role
UNION ALL SELECT 'customers', COUNT(*) FROM customers
UNION ALL SELECT 'equipment', COUNT(*) FROM equipment
UNION ALL SELECT 'orders', COUNT(*) FROM orders;
