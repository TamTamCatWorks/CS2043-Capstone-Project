-- =============================================================================
-- TAMTAMCATWORKS AUCTION SYSTEM — V1.1 SEED DATASET
-- =============================================================================
-- Description: This script populates the database with a high-quality dataset
--              for testing and demonstration.
-- How to load: psql -h localhost -U postgres -d tamtamcatworks -f seed-dataset.sql
-- Note: All preloaded passwords are encrypted using standard BCrypt hashes.
--       - 'admin123' for admin
--       - 'hashed123' for testbidder, testseller
--       - 'buyer123' for buyer1
--       - 'seller123' for seller1
-- =============================================================================

-- Clean up any existing data first to prevent constraint violations
TRUNCATE TABLE bid_transactions CASCADE;
TRUNCATE TABLE auctions CASCADE;
TRUNCATE TABLE art_items CASCADE;
TRUNCATE TABLE electronics_items CASCADE;
TRUNCATE TABLE vehicle_items CASCADE;
TRUNCATE TABLE other_items CASCADE;
TRUNCATE TABLE items CASCADE;
TRUNCATE TABLE admin_permissions CASCADE;
TRUNCATE TABLE admin_action_log CASCADE;
TRUNCATE TABLE "Users" CASCADE;
TRUNCATE TABLE admin_profiles CASCADE;
TRUNCATE TABLE buyer_profiles CASCADE;
TRUNCATE TABLE seller_profiles CASCADE;

-- -----------------------------------------------------------------------------
-- 1. SEED PROFILES
-- -----------------------------------------------------------------------------

-- Admin Profiles
INSERT INTO admin_profiles (id, creation_date)
VALUES ('adm-profile-0001', CURRENT_TIMESTAMP);

-- Buyer Profiles
INSERT INTO buyer_profiles (id, creation_date) VALUES 
('buy-profile-0001', CURRENT_TIMESTAMP),
('buy-profile-0002', CURRENT_TIMESTAMP),
('buy-profile-0003', CURRENT_TIMESTAMP),
('buy-profile-0004', CURRENT_TIMESTAMP);

-- Seller Profiles
INSERT INTO seller_profiles (id, creation_date) VALUES 
('sel-profile-0001', CURRENT_TIMESTAMP),
('sel-profile-0002', CURRENT_TIMESTAMP);

-- -----------------------------------------------------------------------------
-- 2. SEED USERS (Table: "Users" with capital U)
-- -----------------------------------------------------------------------------

INSERT INTO "Users" (id, creation_date, username, email, password_hash, full_name, balance, hold_balance, admin_profile_id, buyer_profile_id, seller_profile_id) VALUES
-- System Admin (pw: 'admin123')
('usr-admin-0001', CURRENT_TIMESTAMP, 'admin', 'admin@example.com', '$2a$10$h9.Rpy6iNqXfL.wB8eNqEuV/t1.6.M60.vE7E2bXFv8tU3t/H3Ryu', 'System Administrator', 0.0, 0.0, 'adm-profile-0001', NULL, NULL),

-- Test Bidder (pw: 'hashed123')
('usr-bidder-0001', CURRENT_TIMESTAMP, 'testbidder', 'test1@example.com', '$2a$10$Xo1Zk3nE3XpA/5eSjK07uO/N5rG5/t/vE7E2bXFv8tU3t/H3Ryu', 'Test Bidder', 48750.0, 1250.0, NULL, 'buy-profile-0001', NULL),

-- Test Seller (pw: 'hashed123')
('usr-seller-0001', CURRENT_TIMESTAMP, 'testseller', 'test2@example.com', '$2a$10$Xo1Zk3nE3XpA/5eSjK07uO/N5rG5/t/vE7E2bXFv8tU3t/H3Ryu', 'Test Seller', 1000.0, 0.0, NULL, 'buy-profile-0002', 'sel-profile-0001'),

-- Buyer 1 (pw: 'buyer123')
('usr-buyer-0001', CURRENT_TIMESTAMP, 'buyer1', 'buyer1@example.com', '$2a$10$aE5o/53g9Wb2C7wJ.NzehOc26oM2sI09oV5UaK8z.F4K1R7qXm2f2', 'Jane Doe', 19000.0, 1000.0, NULL, 'buy-profile-0003', NULL),

-- Seller 1 (pw: 'seller123')
('usr-seller-0002', CURRENT_TIMESTAMP, 'seller1', 'seller1@example.com', '$2a$10$Xo1Zk3nE3XpA/5eSjK07uO/N5rG5/t/vE7E2bXFv8tU3t/H3Ryu', 'John Smith', 2500.0, 0.0, NULL, 'buy-profile-0004', 'sel-profile-0002');

-- -----------------------------------------------------------------------------
-- 3. SEED ADMIN PERMISSIONS & LOGS
-- -----------------------------------------------------------------------------

INSERT INTO admin_permissions (admin_id, permissions) VALUES 
('adm-profile-0001', 'USER_MANAGE'),
('adm-profile-0001', 'AUCTION_MANAGE'),
('adm-profile-0001', 'REPORT_MODERATE'),
('adm-profile-0001', 'AUDIT_VIEW'),
('adm-profile-0001', 'FINANCE_VIEW');

INSERT INTO admin_action_log (admin_id, action_log) VALUES 
('adm-profile-0001', '[2026-05-25 10:00:00] Admin account initialized.'),
('adm-profile-0001', '[2026-05-25 10:15:30] Completed system configuration audit.');

-- -----------------------------------------------------------------------------
-- 4. SEED ITEMS
-- -----------------------------------------------------------------------------

-- Base Items
INSERT INTO items (id, creation_date, name, description, starting_price, condition, image_url, seller_id, dtype) VALUES
-- Art Item
('item-art-0001', CURRENT_TIMESTAMP, 'Starry Night Replica', 'Beautiful hand-painted replica of Van Gogh''s iconic canvas masterpiece.', 850.0, 'GOOD', 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500', 'usr-seller-0001', 'ART'),

-- Electronics Item
('item-elec-0001', CURRENT_TIMESTAMP, 'LG C3 55" OLED TV', 'Experience pitch blacks and high contrast with the ultimate 55-inch smart TV.', 1200.0, 'NEW', 'https://images.unsplash.com/photo-1593305841991-05c297ba4575?w=500', 'usr-seller-0002', 'ELECTRONICS'),

-- Vehicle Item
('item-veh-0001', CURRENT_TIMESTAMP, 'Tesla Model 3 Dual Motor', 'All-wheel drive Long Range edition, Midnight Silver color, active Autopilot package.', 28000.0, 'GOOD', 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=500', 'usr-seller-0002', 'VEHICLE'),

-- Other Item
('item-oth-0001', CURRENT_TIMESTAMP, 'Rare Amazing Fantasy #15 Comic Reprint', 'Mint condition replica reprint of Spider-Man''s classic 1962 debut issue.', 180.0, 'FAIR', 'https://images.unsplash.com/photo-1608889175123-8ec330b86f84?w=500', 'usr-seller-0001', 'OTHER');

-- Subclass Item Details
INSERT INTO art_items (item_id, artist, year, medium, dimensions, has_certificate) VALUES 
('item-art-0001', 'Vincent replica artists', 2024, 'Oil on Canvas', '92cm x 73cm', false);

INSERT INTO electronics_items (item_id, brand, model, warranty_months) VALUES
('item-elec-0001', 'LG Electronics', 'OLED55C3PSA', 24);

INSERT INTO vehicle_items (item_id, make, model, year, mileage_km, color, fuel_type) VALUES
('item-veh-0001', 'Tesla Motors', 'Model 3 Long Range', 2021, 24300, 'Midnight Silver Metallic', 'Electric');

INSERT INTO other_items (item_id) VALUES
('item-oth-0001');

-- -----------------------------------------------------------------------------
-- 5. SEED AUCTIONS
-- -----------------------------------------------------------------------------

INSERT INTO auctions (id, creation_date, title, start_price, current_price, start_time, end_time, status, seller_id, item_id, leading_bidder_id) VALUES
-- Auction 1 (Active art auction with pre-existing bids)
('auc-art-0001', CURRENT_TIMESTAMP, 'Van Gogh Starry Night Painting Replica', 850.0, 1000.0, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP + INTERVAL '5 days', 'ACTIVE', 'usr-seller-0001', 'item-art-0001', 'usr-buyer-0001'),

-- Auction 2 (Active electronics auction with one bid)
('auc-elec-0001', CURRENT_TIMESTAMP, 'LG 55" OLED Smart TV Premium Auction', 1200.0, 1250.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '3 days', 'ACTIVE', 'usr-seller-0002', 'item-elec-0001', 'usr-bidder-0001'),

-- Auction 3 (Pending draft vehicle auction)
('auc-veh-0001', CURRENT_TIMESTAMP, 'Sleek Tesla Model 3 2021 Autopilot Enabled', 28000.0, 28000.0, CURRENT_TIMESTAMP + INTERVAL '2 days', CURRENT_TIMESTAMP + INTERVAL '9 days', 'PENDING', 'usr-seller-0002', 'item-veh-0001', NULL);

-- -----------------------------------------------------------------------------
-- 6. SEED BID TRANSACTIONS
-- -----------------------------------------------------------------------------

INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id) VALUES
-- Bid 1 on Art: Test Bidder bids $900
('bid-tx-0001', CURRENT_TIMESTAMP - INTERVAL '10 hours', 900.0, 'MANUAL', 'auc-art-0001', 'usr-bidder-0001'),

-- Bid 2 on Art: Buyer 1 outbids at $1000 (currently leading)
('bid-tx-0002', CURRENT_TIMESTAMP - INTERVAL '8 hours', 1000.0, 'MANUAL', 'auc-art-0001', 'usr-buyer-0001'),

-- Bid 3 on TV: Test Bidder bids $1250 (currently leading)
('bid-tx-0003', CURRENT_TIMESTAMP - INTERVAL '2 hours', 1250.0, 'MANUAL', 'auc-elec-0001', 'usr-bidder-0001');

-- =============================================================================
-- END OF SEED SCRIPT
-- =============================================================================
