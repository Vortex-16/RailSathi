-- RailSaathi PostgreSQL Production Schema
-- Designed for hosted PostgreSQL (Supabase, Neon, AWS RDS, etc.)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Users / Device Sessions
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id VARCHAR(128) NOT NULL UNIQUE,
    installation_id VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'TRAVELER', -- 'GUEST', 'TRAVELER', 'VENDOR'
    display_name VARCHAR(128),
    phone VARCHAR(32),
    language VARCHAR(32) DEFAULT 'ENGLISH',
    is_senior_mode BOOLEAN DEFAULT FALSE,
    session_token VARCHAR(256) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_device ON users(device_id);
CREATE INDEX IF NOT EXISTS idx_users_session ON users(session_token);

-- 2. Vendors Profile & State
CREATE TABLE IF NOT EXISTS vendors (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    vendor_code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    phone VARCHAR(32),
    speciality_item_name VARCHAR(128) NOT NULL,
    speciality_item_id VARCHAR(64) NOT NULL,
    base_price INTEGER DEFAULT 15,
    is_active BOOLEAN DEFAULT TRUE,
    current_train_number VARCHAR(32),
    current_coach VARCHAR(32) DEFAULT 'GS-2',
    current_station_code VARCHAR(32),
    today_sales_count INTEGER DEFAULT 0,
    today_earnings INTEGER DEFAULT 0,
    last_active_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_vendors_active_train ON vendors(current_train_number, is_active);
CREATE INDEX IF NOT EXISTS idx_vendors_coach ON vendors(current_coach);

-- 3. Journey Sessions
CREATE TABLE IF NOT EXISTS journeys (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    train_number VARCHAR(32) NOT NULL,
    train_name VARCHAR(256) NOT NULL,
    origin_station_code VARCHAR(32) NOT NULL,
    origin_station_name VARCHAR(128) NOT NULL,
    dest_station_code VARCHAR(32) NOT NULL,
    dest_station_name VARCHAR(128) NOT NULL,
    selected_coach VARCHAR(32) NOT NULL DEFAULT 'GS-2',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE', -- 'PLANNED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED'
    started_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_journeys_user ON journeys(user_id, status);
CREATE INDEX IF NOT EXISTS idx_journeys_train ON journeys(train_number, status);

-- 4. Food Catalogue
CREATE TABLE IF NOT EXISTS food_items (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    local_name_bn VARCHAR(128),
    local_name_hi VARCHAR(128),
    local_name_mr VARCHAR(128),
    local_name_ta VARCHAR(128),
    state VARCHAR(64) NOT NULL,
    region VARCHAR(64) NOT NULL,
    category VARCHAR(64) NOT NULL,
    is_vegetarian BOOLEAN DEFAULT TRUE,
    dietary_tags TEXT[] DEFAULT ARRAY['fresh', 'local'],
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_food_items_state ON food_items(state, is_active);

-- 5. Food Requests
CREATE TABLE IF NOT EXISTS food_requests (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_request_id VARCHAR(128) NOT NULL UNIQUE, -- Idempotency key
    customer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    journey_id UUID REFERENCES journeys(id) ON DELETE SET NULL,
    train_number VARCHAR(32) NOT NULL,
    coach_number VARCHAR(32) NOT NULL,
    food_item_id VARCHAR(64) NOT NULL,
    food_item_name VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 1 AND quantity <= 10),
    note VARCHAR(256),
    status VARCHAR(32) NOT NULL DEFAULT 'REQUESTED', -- 'REQUESTED', 'MATCHING', 'OFFERED_TO_VENDOR', 'VENDOR_ACCEPTED', 'PRICE_CONFIRMED', 'CUSTOMER_CONFIRMED', 'FULFILLING', 'COMPLETED', 'REJECTED', 'EXPIRED', 'CUSTOMER_CANCELLED', 'VENDOR_CANCELLED'
    matched_vendor_id UUID REFERENCES vendors(id) ON DELETE SET NULL,
    offered_unit_price INTEGER CHECK (offered_unit_price IS NULL OR offered_unit_price IN (5, 10, 15, 20, 30, 40, 50)),
    calculated_total_price INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE DEFAULT (CURRENT_TIMESTAMP + INTERVAL '5 minutes'),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_food_requests_train_coach ON food_requests(train_number, coach_number, status);
CREATE INDEX IF NOT EXISTS idx_food_requests_vendor ON food_requests(matched_vendor_id);
CREATE INDEX IF NOT EXISTS idx_food_requests_client_id ON food_requests(client_request_id);

-- 6. Orders (Confirmed Transactions)
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    request_id UUID REFERENCES food_requests(id) ON DELETE CASCADE UNIQUE,
    client_order_id VARCHAR(128) NOT NULL UNIQUE,
    customer_id UUID REFERENCES users(id) ON DELETE SET NULL,
    vendor_id UUID REFERENCES vendors(id) ON DELETE SET NULL,
    journey_id UUID REFERENCES journeys(id) ON DELETE SET NULL,
    train_number VARCHAR(32) NOT NULL,
    coach_number VARCHAR(32) NOT NULL,
    food_item_name VARCHAR(128) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price INTEGER NOT NULL CHECK (unit_price IN (5, 10, 15, 20, 30, 40, 50)),
    total_price INTEGER NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'CONFIRMED', -- 'CONFIRMED', 'FULFILLING', 'COMPLETED', 'CANCELLED'
    payment_method VARCHAR(32) DEFAULT 'CASH_ON_DELIVERY',
    payment_status VARCHAR(32) DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_orders_vendor ON orders(vendor_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer_id, status);

-- 7. Order State Transition Events (Audit Trail)
CREATE TABLE IF NOT EXISTS order_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
    from_status VARCHAR(32),
    to_status VARCHAR(32) NOT NULL,
    triggered_by_role VARCHAR(32) NOT NULL,
    triggered_by_user_id UUID,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Offline Sync Queue (For idempotent synchronization)
CREATE TABLE IF NOT EXISTS sync_queue (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    device_id VARCHAR(128) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    operation_type VARCHAR(64) NOT NULL,
    payload JSONB NOT NULL,
    status VARCHAR(32) DEFAULT 'PROCESSED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sync_idempotency ON sync_queue(idempotency_key);
