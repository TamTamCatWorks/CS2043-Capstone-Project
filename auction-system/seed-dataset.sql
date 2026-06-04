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
TRUNCATE TABLE "users" CASCADE;
TRUNCATE TABLE admin_profiles CASCADE;
TRUNCATE TABLE buyer_profile CASCADE;
TRUNCATE TABLE seller_profile CASCADE;

-- -----------------------------------------------------------------------------
-- 1. SEED PROFILES
-- -----------------------------------------------------------------------------

-- Admin Profiles
INSERT INTO admin_profiles (id, creation_date)
VALUES ('adm-profile-0001', CURRENT_TIMESTAMP);

-- Buyer Profiles
INSERT INTO buyer_profile (id, creation_date, total_spent, total_wins) VALUES 
('buy-profile-0001', CURRENT_TIMESTAMP, 0, 0),
('buy-profile-0002', CURRENT_TIMESTAMP, 0, 0),
('buy-profile-0003', CURRENT_TIMESTAMP, 0, 0),
('buy-profile-0004', CURRENT_TIMESTAMP, 0, 0);

-- Seller Profiles
INSERT INTO seller_profile (id, creation_date, rating, rating_count, total_revenue, total_sold) VALUES 
('sel-profile-0001', CURRENT_TIMESTAMP, 4.9, 10, 9000, 100),
('sel-profile-0002', CURRENT_TIMESTAMP, 4.9, 10, 6900, 100);

-- -----------------------------------------------------------------------------
-- 2. SEED USERS (Table: "Users" with capital U)
-- -----------------------------------------------------------------------------

INSERT INTO "users" (id, creation_date, username, email, password_hash, full_name, balance, hold_balance, admin_profile_id, buyer_profile_id, seller_profile_id, active) VALUES
-- System Admin (pw: 'admin123')
('usr-admin-0001', CURRENT_TIMESTAMP, 'admin', 'admin@example.com', '$2a$12$Eqt0Zjc4FJDzLkBOTI9jROHNma2eMdS0VUnrlxyRGrtudDnYtoT2a', 'System Administrator', 0.0, 0.0, 'adm-profile-0001', NULL, NULL, TRUE),

-- Test Bidder (pw: 'hashed123')
('usr-bidder-0001', CURRENT_TIMESTAMP, 'testbidder', 'test1@example.com', '$2a$10$Xo1Zk3nE3XpA/5eSjK07uO/N5rG5/t/vE7E2bXFv8tU3t/H3Ryu', 'Test Bidder', 48750.0, 1250.0, NULL, 'buy-profile-0001', NULL, TRUE),

-- Test Seller (pw: 'hashed123')
('usr-seller-0001', CURRENT_TIMESTAMP, 'testseller', 'test2@example.com', '$2a$10$Xo1Zk3nE3XpA/5eSjK07uO/N5rG5/t/vE7E2bXFv8tU3t/H3Ryu', 'Test Seller', 1000.0, 0.0, NULL, 'buy-profile-0002', 'sel-profile-0001', TRUE),

-- Buyer 1 (pw: 'buyer123')
('usr-buyer-0001', CURRENT_TIMESTAMP, 'buyer1', 'buyer1@example.com', '$2a$12$x4O7ScKKqXFcpQY49mHbPuZiQPg1c6qArzHtXR5cMp66b/2MZOq9e', 'Jane Doe', 19000.0, 1000.0, NULL, 'buy-profile-0003', NULL, TRUE),

-- Seller 1 (pw: 'seller123')
('usr-seller-0002', CURRENT_TIMESTAMP, 'seller1', 'seller1@example.com', '$2a$10$Xo1Zk3nE3XpA/5eSjK07uO/N5rG5/t/vE7E2bXFv8tU3t/H3Ryu', 'John Smith', 2500.0, 0.0, NULL, 'buy-profile-0004', 'sel-profile-0002', TRUE);

-- -----------------------------------------------------------------------------
-- 3. SEED ADMIN PERMISSIONS & LOGS
-- -----------------------------------------------------------------------------

INSERT INTO admin_permissions (admin_id, permissions) VALUES 
('adm-profile-0001', 'MANAGE_USERS'),
('adm-profile-0001', 'MANAGE_ITEMS'),
('adm-profile-0001', 'MANAGE_AUCTIONS'),
('adm-profile-0001', 'VIEW_LOGS'),
('adm-profile-0001', 'MANAGE_ADMINS');

INSERT INTO admin_action_log (admin_id, action_log) VALUES 
('adm-profile-0001', '[2026-05-25 10:00:00] Admin account initialized.'),
('adm-profile-0001', '[2026-05-25 10:15:30] Completed system configuration audit.');

-- -----------------------------------------------------------------------------
-- 4. SEED ITEMS
-- -----------------------------------------------------------------------------

DO $$
DECLARE
	i int;
	suffix text;
	seller_id text;
	status text;
	starting_price numeric;
	current_price numeric;
	start_time timestamp;
	end_time timestamp;
	item_id text;
	auction_id text;
	title text;
	description text;
	image_url text;
	bidder_one text;
	bidder_two text;
	lead_bidder text;
	bid_step numeric;
	bid_count int;
	artist text;
	year_created int;
	medium text;
	dimensions text;
	has_certificate boolean;
	brand text;
	model text;
	warranty_months int;
	make text;
	vehicle_model text;
	manufacture_year int;
	mileage_km int;
	color text;
	fuel_type text;
BEGIN
	FOR i IN 2..26 LOOP
		suffix := lpad(i::text, 4, '0');
		item_id := 'item-art-' || suffix;
		auction_id := 'auc-art-' || suffix;
		seller_id := CASE WHEN MOD(i, 2) = 0 THEN 'usr-seller-0001' ELSE 'usr-seller-0002' END;
		status := CASE WHEN MOD(i, 5) = 0 THEN 'PENDING' WHEN MOD(i, 5) = 3 THEN 'CLOSED' ELSE 'ACTIVE' END;
		starting_price := 650 + (i * 40);
		bid_step := 50;

		IF status = 'PENDING' THEN
			start_time := CURRENT_TIMESTAMP + make_interval(days => 1 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 6 + MOD(i, 5));
			bid_count := 0;
			current_price := starting_price;
			lead_bidder := NULL;
		ELSIF status = 'CLOSED' THEN
			start_time := CURRENT_TIMESTAMP - make_interval(days => 7 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP - make_interval(days => 1 + MOD(i, 3));
			bid_count := 2;
			current_price := starting_price + (bid_step * 2);
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		ELSE
			start_time := CURRENT_TIMESTAMP - make_interval(days => 2 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 3 + MOD(i, 5));
			bid_count := 1;
			current_price := starting_price + bid_step;
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		END IF;

		title := CASE MOD(i, 5)
			WHEN 0 THEN 'Gallery Study'
			WHEN 1 THEN 'Nocturne Canvas'
			WHEN 2 THEN 'Frame Study'
			WHEN 3 THEN 'Museum Tribute'
			ELSE 'Palette Collector'
		END || ' ' || suffix;

		description := format('Fine art lot %s from the rotating gallery collection.', suffix);
		image_url := CASE MOD(i, 5)
			WHEN 0 THEN 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500'
			WHEN 1 THEN 'https://images.unsplash.com/photo-1578321272176-b7bbc0679853?w=500'
			WHEN 2 THEN 'https://images.unsplash.com/photo-1515405295579-ba7b45403062?w=500'
			WHEN 3 THEN 'https://images.unsplash.com/photo-1547891654-e66ed7ebb968?w=500'
			ELSE 'https://images.unsplash.com/photo-1487412912498-0447578fcca8?w=500'
		END;

		artist := CASE MOD(i, 5)
			WHEN 0 THEN 'Mara Vale'
			WHEN 1 THEN 'Theo Finch'
			WHEN 2 THEN 'Iris Delorme'
			WHEN 3 THEN 'Jun Sato'
			ELSE 'Elena Costa'
		END;

		year_created := 1980 + MOD(i, 44);

		medium := CASE MOD(i, 5)
			WHEN 0 THEN 'Oil on Canvas'
			WHEN 1 THEN 'Acrylic on Linen'
			WHEN 2 THEN 'Watercolor Paper'
			WHEN 3 THEN 'Mixed Media'
			ELSE 'Ink and Gold Leaf'
		END;

		dimensions := CASE MOD(i, 5)
			WHEN 0 THEN '60cm x 80cm'
			WHEN 1 THEN '70cm x 90cm'
			WHEN 2 THEN '50cm x 65cm'
			WHEN 3 THEN '100cm x 75cm'
			ELSE '45cm x 60cm'
		END;

		has_certificate := MOD(i, 2) = 0;

		INSERT INTO items (id, creation_date, name, description, starting_price, condition, image_url, seller_id, item_type, listed_at)
		VALUES (
			item_id,
			CURRENT_TIMESTAMP - make_interval(days => i),
			title,
			description,
			starting_price,
			CASE MOD(i, 5)
				WHEN 0 THEN 'NEW'
				WHEN 1 THEN 'LIKE_NEW'
				WHEN 2 THEN 'GOOD'
				WHEN 3 THEN 'FAIR'
				ELSE 'POOR'
			END,
			image_url,
			seller_id,
			'ART',
			CURRENT_TIMESTAMP - make_interval(days => i)
		);

		INSERT INTO art_items (item_id, artist, year_created, medium, dimensions, has_certificate)
		VALUES (item_id, artist, year_created, medium, dimensions, has_certificate);

		INSERT INTO auctions (id, creation_date, title, starting_price, current_price, start_time, end_time, status, seller_id, item_id, leading_bidder_id, minimum_increment, version)
		VALUES (
			auction_id,
			CURRENT_TIMESTAMP - make_interval(hours => i),
			title || ' Auction',
			starting_price,
			current_price,
			start_time,
			end_time,
			status,
			seller_id,
			item_id,
			lead_bidder,
			bid_step,
			1
		);

		IF status = 'ACTIVE' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-art-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i),
				current_price,
				'MANUAL',
				auction_id,
				bidder_one
			);
		ELSIF status = 'CLOSED' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
			bidder_two := CASE WHEN bidder_one = 'usr-bidder-0001' THEN 'usr-buyer-0001' ELSE 'usr-bidder-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-art-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i + 2),
				starting_price + bid_step,
				'MANUAL',
				auction_id,
				bidder_one
			);

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-art-' || suffix || '-2',
				CURRENT_TIMESTAMP - make_interval(hours => i + 1),
				current_price,
				'MANUAL',
				auction_id,
				bidder_two
			);
		END IF;
	END LOOP;

	FOR i IN 2..26 LOOP
		suffix := lpad(i::text, 4, '0');
		item_id := 'item-elec-' || suffix;
		auction_id := 'auc-elec-' || suffix;
		seller_id := CASE WHEN MOD(i, 2) = 0 THEN 'usr-seller-0001' ELSE 'usr-seller-0002' END;
		status := CASE WHEN MOD(i, 5) = 0 THEN 'PENDING' WHEN MOD(i, 5) = 3 THEN 'CLOSED' ELSE 'ACTIVE' END;
		starting_price := 320 + (i * 75);
		bid_step := 100;

		IF status = 'PENDING' THEN
			start_time := CURRENT_TIMESTAMP + make_interval(days => 1 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 5 + MOD(i, 5));
			bid_count := 0;
			current_price := starting_price;
			lead_bidder := NULL;
		ELSIF status = 'CLOSED' THEN
			start_time := CURRENT_TIMESTAMP - make_interval(days => 5 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP - make_interval(hours => 6 + MOD(i, 5));
			bid_count := 2;
			current_price := starting_price + (bid_step * 2);
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		ELSE
			start_time := CURRENT_TIMESTAMP - make_interval(days => 1 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 2 + MOD(i, 5));
			bid_count := 1;
			current_price := starting_price + bid_step;
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		END IF;

		title := CASE MOD(i, 5)
			WHEN 0 THEN 'OLED Showcase'
			WHEN 1 THEN 'Soundbar Bundle'
			WHEN 2 THEN 'Workspace Display'
			WHEN 3 THEN 'Gaming Rig'
			ELSE 'Home Cinema'
		END || ' ' || suffix;

		description := format('Electronics lot %s with rotating consumer-grade features and accessories.', suffix);
		image_url := CASE MOD(i, 5)
			WHEN 0 THEN 'https://images.unsplash.com/photo-1593305841991-05c297ba4575?w=500'
			WHEN 1 THEN 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=500'
			WHEN 2 THEN 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=500'
			WHEN 3 THEN 'https://images.unsplash.com/photo-1545239351-1141bd82e8a6?w=500'
			ELSE 'https://images.unsplash.com/photo-1550009158-9ebf69173e03?w=500'
		END;

		brand := CASE MOD(i, 5)
			WHEN 0 THEN 'LG Electronics'
			WHEN 1 THEN 'Sony'
			WHEN 2 THEN 'Samsung'
			WHEN 3 THEN 'Apple'
			ELSE 'Dell'
		END;

		model := CASE MOD(i, 5)
			WHEN 0 THEN 'OLED77C3'
			WHEN 1 THEN 'Bravia XR-65'
			WHEN 2 THEN 'Neo QLED QN90'
			WHEN 3 THEN 'MacBook Pro 14'
			ELSE 'UltraSharp U2723'
		END;

		warranty_months := CASE MOD(i, 5)
			WHEN 0 THEN 12
			WHEN 1 THEN 24
			WHEN 2 THEN 36
			WHEN 3 THEN 48
			ELSE 60
		END;

		INSERT INTO items (id, creation_date, name, description, starting_price, condition, image_url, seller_id, item_type, listed_at)
		VALUES (
			item_id,
			CURRENT_TIMESTAMP - make_interval(days => i + 1),
			title,
			description,
			starting_price,
			CASE MOD(i, 5)
				WHEN 0 THEN 'NEW'
				WHEN 1 THEN 'LIKE_NEW'
				WHEN 2 THEN 'GOOD'
				WHEN 3 THEN 'FAIR'
				ELSE 'POOR'
			END,
			image_url,
			seller_id,
			'ELECTRONICS',
			CURRENT_TIMESTAMP - make_interval(days => i + 1)
		);

		INSERT INTO electronics_items (item_id, brand, model, warranty_months)
		VALUES (item_id, brand, model, warranty_months);

		INSERT INTO auctions (id, creation_date, title, starting_price, current_price, start_time, end_time, status, seller_id, item_id, leading_bidder_id, minimum_increment, version)
		VALUES (
			auction_id,
			CURRENT_TIMESTAMP - make_interval(hours => i),
			title || ' Auction',
			starting_price,
			current_price,
			start_time,
			end_time,
			status,
			seller_id,
			item_id,
			lead_bidder,
			bid_step,
			1
		);

		IF status = 'ACTIVE' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-elec-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i),
				current_price,
				'MANUAL',
				auction_id,
				bidder_one
			);
		ELSIF status = 'CLOSED' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
			bidder_two := CASE WHEN bidder_one = 'usr-bidder-0001' THEN 'usr-buyer-0001' ELSE 'usr-bidder-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-elec-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i + 2),
				starting_price + bid_step,
				'MANUAL',
				auction_id,
				bidder_one
			);

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-elec-' || suffix || '-2',
				CURRENT_TIMESTAMP - make_interval(hours => i + 1),
				current_price,
				'MANUAL',
				auction_id,
				bidder_two
			);
		END IF;
	END LOOP;

	FOR i IN 2..26 LOOP
		suffix := lpad(i::text, 4, '0');
		item_id := 'item-veh-' || suffix;
		auction_id := 'auc-veh-' || suffix;
		seller_id := CASE WHEN MOD(i, 2) = 0 THEN 'usr-seller-0001' ELSE 'usr-seller-0002' END;
		status := CASE WHEN MOD(i, 5) = 0 THEN 'PENDING' WHEN MOD(i, 5) = 3 THEN 'CLOSED' ELSE 'ACTIVE' END;
		starting_price := 8800 + (i * 1200);
		bid_step := 1000;

		IF status = 'PENDING' THEN
			start_time := CURRENT_TIMESTAMP + make_interval(days => 2 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 9 + MOD(i, 5));
			bid_count := 0;
			current_price := starting_price;
			lead_bidder := NULL;
		ELSIF status = 'CLOSED' THEN
			start_time := CURRENT_TIMESTAMP - make_interval(days => 10 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP - make_interval(days => 2 + MOD(i, 3));
			bid_count := 2;
			current_price := starting_price + (bid_step * 2);
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		ELSE
			start_time := CURRENT_TIMESTAMP - make_interval(days => 3 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 4 + MOD(i, 5));
			bid_count := 1;
			current_price := starting_price + bid_step;
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		END IF;

		title := CASE MOD(i, 5)
			WHEN 0 THEN 'Urban Sedan'
			WHEN 1 THEN 'Long-Range Crossover'
			WHEN 2 THEN 'Weekend Coupe'
			WHEN 3 THEN 'Family SUV'
			ELSE 'City Hatchback'
		END || ' ' || suffix;

		description := format('Vehicle lot %s selected for the showcase garage rotation.', suffix);
		image_url := CASE MOD(i, 5)
			WHEN 0 THEN 'https://images.unsplash.com/photo-1617788138017-80ad40651399?w=500'
			WHEN 1 THEN 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?w=500'
			WHEN 2 THEN 'https://images.unsplash.com/photo-1494976388531-d1058494cdd8?w=500'
			WHEN 3 THEN 'https://images.unsplash.com/photo-1550355291-bbee04a92027?w=500'
			ELSE 'https://images.unsplash.com/photo-1542362567-b07e54358753?w=500'
		END;

		make := CASE MOD(i, 5)
			WHEN 0 THEN 'Tesla Motors'
			WHEN 1 THEN 'Toyota'
			WHEN 2 THEN 'BMW'
			WHEN 3 THEN 'Ford'
			ELSE 'Hyundai'
		END;

		vehicle_model := CASE MOD(i, 5)
			WHEN 0 THEN 'Model Y'
			WHEN 1 THEN 'Camry Hybrid'
			WHEN 2 THEN 'i4 eDrive'
			WHEN 3 THEN 'Mustang Mach-E'
			ELSE 'Ioniq 5'
		END;

		manufacture_year := 2017 + MOD(i, 8);
		mileage_km := 9000 + (i * 2200);

		color := CASE MOD(i, 5)
			WHEN 0 THEN 'Midnight Silver Metallic'
			WHEN 1 THEN 'Pearl White'
			WHEN 2 THEN 'Black Sapphire'
			WHEN 3 THEN 'Velocity Blue'
			ELSE 'Lucid Blue'
		END;

		fuel_type := CASE MOD(i, 5)
			WHEN 0 THEN 'Electric'
			WHEN 1 THEN 'Hybrid'
			WHEN 2 THEN 'Petrol'
			WHEN 3 THEN 'Electric'
			ELSE 'Plug-in Hybrid'
		END;

		INSERT INTO items (id, creation_date, name, description, starting_price, condition, image_url, seller_id, item_type, listed_at)
		VALUES (
			item_id,
			CURRENT_TIMESTAMP - make_interval(days => i + 2),
			title,
			description,
			starting_price,
			CASE MOD(i, 5)
				WHEN 0 THEN 'NEW'
				WHEN 1 THEN 'LIKE_NEW'
				WHEN 2 THEN 'GOOD'
				WHEN 3 THEN 'FAIR'
				ELSE 'POOR'
			END,
			image_url,
			seller_id,
			'VEHICLE',
			CURRENT_TIMESTAMP - make_interval(days => i + 2)
		);

		INSERT INTO vehicle_items (item_id, make, model, manufacture_year, mileage_km, color, fuel_type)
		VALUES (item_id, make, vehicle_model, manufacture_year, mileage_km, color, fuel_type);

		INSERT INTO auctions (id, creation_date, title, starting_price, current_price, start_time, end_time, status, seller_id, item_id, leading_bidder_id, minimum_increment, version)
		VALUES (
			auction_id,
			CURRENT_TIMESTAMP - make_interval(hours => i),
			title || ' Auction',
			starting_price,
			current_price,
			start_time,
			end_time,
			status,
			seller_id,
			item_id,
			lead_bidder,
			bid_step,
			1
		);

		IF status = 'ACTIVE' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-veh-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i),
				current_price,
				'MANUAL',
				auction_id,
				bidder_one
			);
		ELSIF status = 'CLOSED' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
			bidder_two := CASE WHEN bidder_one = 'usr-bidder-0001' THEN 'usr-buyer-0001' ELSE 'usr-bidder-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-veh-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i + 2),
				starting_price + bid_step,
				'MANUAL',
				auction_id,
				bidder_one
			);

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-veh-' || suffix || '-2',
				CURRENT_TIMESTAMP - make_interval(hours => i + 1),
				current_price,
				'MANUAL',
				auction_id,
				bidder_two
			);
		END IF;
	END LOOP;

	FOR i IN 2..26 LOOP
		suffix := lpad(i::text, 4, '0');
		item_id := 'item-oth-' || suffix;
		auction_id := 'auc-oth-' || suffix;
		seller_id := CASE WHEN MOD(i, 2) = 0 THEN 'usr-seller-0001' ELSE 'usr-seller-0002' END;
		status := CASE WHEN MOD(i, 5) = 0 THEN 'PENDING' WHEN MOD(i, 5) = 3 THEN 'CLOSED' ELSE 'ACTIVE' END;
		starting_price := 90 + (i * 18);
		bid_step := 20;

		IF status = 'PENDING' THEN
			start_time := CURRENT_TIMESTAMP + make_interval(days => 1 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 4 + MOD(i, 5));
			bid_count := 0;
			current_price := starting_price;
			lead_bidder := NULL;
		ELSIF status = 'CLOSED' THEN
			start_time := CURRENT_TIMESTAMP - make_interval(days => 4 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP - make_interval(days => 1 + MOD(i, 3));
			bid_count := 2;
			current_price := starting_price + (bid_step * 2);
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		ELSE
			start_time := CURRENT_TIMESTAMP - make_interval(days => 1 + MOD(i, 4));
			end_time := CURRENT_TIMESTAMP + make_interval(days => 2 + MOD(i, 5));
			bid_count := 1;
			current_price := starting_price + bid_step;
			lead_bidder := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
		END IF;

		title := CASE MOD(i, 5)
			WHEN 0 THEN 'Vintage Poster'
			WHEN 1 THEN 'Collector Comic'
			WHEN 2 THEN 'Retro Vinyl'
			WHEN 3 THEN 'Signed Book'
			ELSE 'Desk Artifact'
		END || ' ' || suffix;

		description := format('Miscellaneous collectible lot %s with distinctive shelf appeal.', suffix);
		image_url := CASE MOD(i, 5)
			WHEN 0 THEN 'https://images.unsplash.com/photo-1608889175123-8ec330b86f84?w=500'
			WHEN 1 THEN 'https://images.unsplash.com/photo-1517048676732-d65bc937f952?w=500'
			WHEN 2 THEN 'https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=500'
			WHEN 3 THEN 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500'
			ELSE 'https://images.unsplash.com/photo-1491553895911-0055eca6402d?w=500'
		END;

		INSERT INTO items (id, creation_date, name, description, starting_price, condition, image_url, seller_id, item_type, listed_at)
		VALUES (
			item_id,
			CURRENT_TIMESTAMP - make_interval(days => i + 3),
			title,
			description,
			starting_price,
			CASE MOD(i, 5)
				WHEN 0 THEN 'NEW'
				WHEN 1 THEN 'LIKE_NEW'
				WHEN 2 THEN 'GOOD'
				WHEN 3 THEN 'FAIR'
				ELSE 'POOR'
			END,
			image_url,
			seller_id,
			'OTHER',
			CURRENT_TIMESTAMP - make_interval(days => i + 3)
		);

		INSERT INTO other_items (item_id) VALUES (item_id);

		INSERT INTO auctions (id, creation_date, title, starting_price, current_price, start_time, end_time, status, seller_id, item_id, leading_bidder_id, minimum_increment, version)
		VALUES (
			auction_id,
			CURRENT_TIMESTAMP - make_interval(hours => i),
			title || ' Auction',
			starting_price,
			current_price,
			start_time,
			end_time,
			status,
			seller_id,
			item_id,
			lead_bidder,
			bid_step,
			1
		);

		IF status = 'ACTIVE' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-oth-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i),
				current_price,
				'MANUAL',
				auction_id,
				bidder_one
			);
		ELSIF status = 'CLOSED' THEN
			bidder_one := CASE WHEN MOD(i, 2) = 0 THEN 'usr-bidder-0001' ELSE 'usr-buyer-0001' END;
			bidder_two := CASE WHEN bidder_one = 'usr-bidder-0001' THEN 'usr-buyer-0001' ELSE 'usr-bidder-0001' END;

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-oth-' || suffix || '-1',
				CURRENT_TIMESTAMP - make_interval(hours => i + 2),
				starting_price + bid_step,
				'MANUAL',
				auction_id,
				bidder_one
			);

			INSERT INTO bid_transactions (id, creation_date, amount, bid_type, auction_id, bidder_id)
			VALUES (
				'bid-oth-' || suffix || '-2',
				CURRENT_TIMESTAMP - make_interval(hours => i + 1),
				current_price,
				'MANUAL',
				auction_id,
				bidder_two
			);
		END IF;
	END LOOP;
END $$;

-- -----------------------------------------------------------------------------
-- 5. SEED AUCTIONS
-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- 6. SEED BID TRANSACTIONS
-- -----------------------------------------------------------------------------

-- =============================================================================
-- END OF SEED SCRIPT
-- =============================================================================
