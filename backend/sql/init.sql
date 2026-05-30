-- =====================================================
-- Order Manager System - PostgreSQL Init Script
-- Version: 1.0
-- =====================================================

\c order_manager_db;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 3. User Role Table
DROP TABLE IF EXISTS sys_user_role CASCADE;
CREATE TABLE sys_user_role (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    role_desc TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'normal',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_sys_user_role_username ON sys_user_role(username);
CREATE INDEX idx_sys_user_role_role_name ON sys_user_role(role_name);
CREATE INDEX idx_sys_user_role_status ON sys_user_role(status);

-- 4. Customer Table
DROP TABLE IF EXISTS customers CASCADE;
CREATE TABLE customers (
    customer_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_name VARCHAR(100) NOT NULL UNIQUE,
    contact_person VARCHAR(100),
    contact_phone VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'normal',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_customers_name ON customers(customer_name);
CREATE UNIQUE INDEX idx_customers_phone ON customers(contact_phone);
CREATE INDEX idx_customers_status ON customers(status);

-- 5. Equipment Table
DROP TABLE IF EXISTS equipment CASCADE;
CREATE TABLE equipment (
    equipment_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    equipment_code VARCHAR(50) NOT NULL UNIQUE,
    equipment_name VARCHAR(100) NOT NULL,
    specification VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'normal',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_equipment_code ON equipment(equipment_code);
CREATE INDEX idx_equipment_status ON equipment(status);

-- 6. Order Table
DROP TABLE IF EXISTS orders CASCADE;
CREATE TABLE orders (
    order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL,
    equipment_id UUID NOT NULL,
    order_type VARCHAR(20) NOT NULL,
    order_amount NUMERIC(10, 2) NOT NULL,
    delivery_date DATE NOT NULL,
    order_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    remarks TEXT,
    created_by UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Foreign Keys
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_customer
    FOREIGN KEY (customer_id)
    REFERENCES customers(customer_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_equipment
    FOREIGN KEY (equipment_id)
    REFERENCES equipment(equipment_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_creator
    FOREIGN KEY (created_by)
    REFERENCES sys_user_role(user_id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE;

-- Indexes
CREATE INDEX idx_orders_customer_equipment_date
    ON orders(customer_id, equipment_id, DATE(created_at))
    WHERE deleted = FALSE;

CREATE INDEX idx_orders_status ON orders(order_status) WHERE deleted = FALSE;
CREATE INDEX idx_orders_created_at ON orders(created_at DESC) WHERE deleted = FALSE;

-- 7. Insert Test Data

-- 7.1 User Roles (with fixed UUIDs for foreign key references)
INSERT INTO sys_user_role (user_id, username, password, role_name, role_desc, status) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'admin', '123456', 'admin', 'System Administrator', 'normal'),
    ('a0000000-0000-0000-0000-000000000002', 'operator', '123456', 'operator', 'Normal Operator', 'normal');

-- 7.2 Customer Data (with fixed UUIDs for foreign key references)
INSERT INTO customers (customer_id, customer_name, contact_person, contact_phone, status) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'Huawei Technologies', 'Zhang San', '13800138001', 'normal'),
    ('c0000000-0000-0000-0000-000000000002', 'Xiaomi Technology', 'Li Si', '13800138002', 'normal'),
    ('c0000000-0000-0000-0000-000000000003', 'Alibaba Group', 'Wang Wu', '13800138003', 'normal'),
    ('c0000000-0000-0000-0000-000000000004', 'Tencent Computer', 'Zhao Liu', '13800138004', 'normal'),
    ('c0000000-0000-0000-0000-000000000005', 'ByteDance Tech', 'Sun Qi', '13800138005', 'normal'),
    ('c0000000-0000-0000-0000-000000000006', 'JD.com', 'Zhou Ba', '13800138006', 'normal'),
    ('c0000000-0000-0000-0000-000000000007', 'NetEase', 'Wu Jiu', '13800138007', 'normal'),
    ('c0000000-0000-0000-0000-000000000008', 'Meituan', 'Zheng Shi', '13800138008', 'normal');

-- 7.3 Equipment Data (with fixed UUIDs for foreign key references)
INSERT INTO equipment (equipment_id, equipment_code, equipment_name, specification, status) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'EQ-001', 'Server Rack', '42U Standard Rack, Dual Power', 'normal'),
    ('e0000000-0000-0000-0000-000000000002', 'EQ-002', 'Core Switch', '48-Port 10G Switch', 'normal'),
    ('e0000000-0000-0000-0000-000000000003', 'EQ-003', 'Firewall', 'NG Firewall, 10Gbps', 'normal'),
    ('e0000000-0000-0000-0000-000000000004', 'EQ-004', 'Storage Array', 'All-Flash 100TB', 'normal'),
    ('e0000000-0000-0000-0000-000000000005', 'EQ-005', 'Precision AC', '20KW Precision AC', 'normal'),
    ('e0000000-0000-0000-0000-000000000006', 'EQ-006', 'UPS Power', 'Online UPS 20KVA', 'repair'),
    ('e0000000-0000-0000-0000-000000000007', 'EQ-007', 'Blade Server', '256-Core Blade', 'normal'),
    ('e0000000-0000-0000-0000-000000000008', 'EQ-008', 'Load Balancer', 'ADX 4-Layer 7-Layer', 'normal'),
    ('e0000000-0000-0000-0000-000000000009', 'EQ-009', 'Fiber Switch', '16-Port Fiber Switch', 'normal'),
    ('e0000000-0000-0000-0000-000000000010', 'EQ-010', 'Monitor Camera', '4K HD Camera', 'disabled');

-- 7.4 Sample Order Data (fixed UUIDs for data integrity)
INSERT INTO orders (customer_id, equipment_id, order_type, order_amount, delivery_date, order_status, remarks, created_by, deleted) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000001', 'sales', 85000.00, CURRENT_DATE + INTERVAL '5 days', 'approved', 'Huawei rack deployment', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000002', 'sales', 42000.00, CURRENT_DATE + INTERVAL '7 days', 'approved', 'Xiaomi network upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000003', 'purchase', 128000.00, CURRENT_DATE + INTERVAL '10 days', 'pending', 'Alibaba security refresh', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000004', 'sales', 350000.00, CURRENT_DATE + INTERVAL '14 days', 'approved', 'Tencent storage expansion', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000005', 'purchase', 65000.00, CURRENT_DATE + INTERVAL '3 days', 'shipped', 'ByteDance cooling upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000007', 'sales', 520000.00, CURRENT_DATE + INTERVAL '20 days', 'pending', 'Huawei compute cluster', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000008', 'sales', 88000.00, CURRENT_DATE + INTERVAL '8 days', 'completed', 'Xiaomi load balancer', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000001', 'purchase', 72000.00, CURRENT_DATE + INTERVAL '12 days', 'approved', 'Alibaba rack expansion', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000009', 'sales', 33000.00, CURRENT_DATE + INTERVAL '6 days', 'pending', 'Tencent SAN upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000003', 'purchase', 198000.00, CURRENT_DATE + INTERVAL '15 days', 'approved', 'ByteDance firewall', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000006', 'e0000000-0000-0000-0000-000000000002', 'sales', 55000.00, CURRENT_DATE + INTERVAL '9 days', 'shipped', 'JD.com core network', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000007', 'e0000000-0000-0000-0000-000000000004', 'sales', 280000.00, CURRENT_DATE + INTERVAL '18 days', 'pending', 'NetEase storage upgrade', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000008', 'e0000000-0000-0000-0000-000000000005', 'purchase', 48000.00, CURRENT_DATE + INTERVAL '4 days', 'completed', 'Meituan DC cooling', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000001', 'e0000000-0000-0000-0000-000000000008', 'sales', 120000.00, CURRENT_DATE + INTERVAL '11 days', 'approved', 'Huawei load balancer', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000002', 'e0000000-0000-0000-0000-000000000007', 'sales', 680000.00, CURRENT_DATE + INTERVAL '25 days', 'pending', 'Xiaomi AI cluster', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000003', 'e0000000-0000-0000-0000-000000000009', 'purchase', 29000.00, CURRENT_DATE + INTERVAL '2 days', 'shipped', 'Alibaba SAN expansion', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000004', 'e0000000-0000-0000-0000-000000000001', 'sales', 95000.00, CURRENT_DATE + INTERVAL '13 days', 'completed', 'Tencent rack build-out', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000005', 'e0000000-0000-0000-0000-000000000002', 'purchase', 38000.00, CURRENT_DATE + INTERVAL '1 day', 'approved', 'ByteDance switch stack', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000006', 'e0000000-0000-0000-0000-000000000004', 'sales', 410000.00, CURRENT_DATE + INTERVAL '22 days', 'pending', 'JD.com storage logistics', 'a0000000-0000-0000-0000-000000000001', false),
    ('c0000000-0000-0000-0000-000000000007', 'e0000000-0000-0000-0000-000000000008', 'sales', 105000.00, CURRENT_DATE + INTERVAL '7 days', 'approved', 'NetEase gaming LB', 'a0000000-0000-0000-0000-000000000001', false);

-- 8. Create Sequence
CREATE SEQUENCE IF NOT EXISTS seq_order_number START 100001;

-- 9. Verify Data
SELECT 'sys_user_role count: ' || COUNT(*) FROM sys_user_role;
SELECT 'customers count: ' || COUNT(*) FROM customers;
SELECT 'equipment count: ' || COUNT(*) FROM equipment;
SELECT 'orders count: ' || COUNT(*) FROM orders;
