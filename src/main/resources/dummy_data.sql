-- =============================================================================
-- dummy_data.sql
-- Smart Home Backend - Dummy Data for Development & AI Model Training
-- Database: MySQL 8.0, Schema: smart_home
--
-- Contents:
--   1. USE statement to select the smart_home database
--   2. UPDATE statements to fix NULL watt values for devices 5, 8, 10
--   3. device_analytics: 30 days of energy usage data for all 11 devices
--      (multiple sessions per day, realistic durations and power values)
--   4. user_behavior_logs: Repeated behavioral patterns for AI predictions
--      (6-8 occurrences each, covering common daily routines)
--
-- Device Reference:
--   1  = HALL LIGHT 1          (LIGHT,  9W)
--   2  = HALL FAN 1            (FAN,   35W)
--   3  = BADROOM LIGHT         (LIGHT,  9W)
--   4  = BADROOM FAN1          (FAN,   35W)
--   5  = AC BADROOM            (AC,  1500W)
--   6  = BADROOM LIGHT TABLE LUMP (LIGHT, 9W)
--   7  = HALL FAN2             (FAN,   35W)
--   8  = HALL LIGHT2           (LIGHT, 12W)
--   9  = KITCHEN LIHGT         (LIGHT,  9W)
--  10  = HALL DOOR LOCK        (LOCK,   5W)
--  11  = REFRIGRATOR           (OTHER, 89W)
--
-- All devices owned by user_id = 4 (admin)
-- =============================================================================

USE smart_home;

-- -----------------------------------------------------------------------------
-- Fix NULL watt values for devices 5 (AC), 8 (HALL LIGHT2), 10 (DOOR LOCK)
-- -----------------------------------------------------------------------------
UPDATE devices SET watt = 1500 WHERE id = 5 AND (watt IS NULL OR watt = 0);
UPDATE devices SET watt = 12   WHERE id = 8 AND (watt IS NULL OR watt = 0);
UPDATE devices SET watt = 5    WHERE id = 10 AND (watt IS NULL OR watt = 0);

-- =============================================================================
-- DEVICE ANALYTICS
-- 30 days of energy usage data (days 0-28 ago) for all 11 devices
-- Columns: device_id, power (watts), duration (minutes), recorded_at
-- =============================================================================

INSERT INTO device_analytics (device_id, power, duration, recorded_at) VALUES

-- -------------------------------------------------------------------------
-- Device 1: HALL LIGHT 1 (LIGHT, 9W) - evening/night sessions
-- -------------------------------------------------------------------------
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 9, 90,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 9, 30,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 9, 75,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 9, 120, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(1, 9, 30,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 9, 90,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 9, 75,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(1, 9, 30,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(1, 9, 90,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, 9, 120, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(1, 9, 75,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(1, 9, 30,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(1, 9, 90,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(1, 9, 30,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(1, 9, 75,  DATE_SUB(NOW(), INTERVAL 19 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(1, 9, 90,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(1, 9, 30,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(1, 9, 120, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(1, 9, 60,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(1, 9, 45,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(1, 9, 75,  DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 2: HALL FAN 1 (FAN, 35W) - daytime/evening sessions
-- -------------------------------------------------------------------------
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 35, 120, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(2, 35, 150, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 35, 120, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(2, 35, 150, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(2, 35, 120, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(2, 35, 150, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(2, 35, 120, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(2, 35, 150, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(2, 35, 240, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(2, 35, 120, DATE_SUB(NOW(), INTERVAL 26 DAY)),
(2, 35, 300, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(2, 35, 180, DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 3: BADROOM LIGHT (LIGHT, 9W) - morning/night sessions
-- -------------------------------------------------------------------------
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(3, 9, 45,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 9, 90,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, 9, 75,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 9, 45,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 9, 90,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(3, 9, 75,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3, 9, 45,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(3, 9, 90,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(3, 9, 75,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(3, 9, 45,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(3, 9, 90,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 19 DAY)),
(3, 9, 75,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(3, 9, 45,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(3, 9, 90,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 25 DAY)),
(3, 9, 60,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(3, 9, 75,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(3, 9, 30,  DATE_SUB(NOW(), INTERVAL 28 DAY));

INSERT INTO device_analytics (device_id, power, duration, recorded_at) VALUES

-- -------------------------------------------------------------------------
-- Device 4: BADROOM FAN1 (FAN, 35W) - night sessions
-- -------------------------------------------------------------------------
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 35, 180, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 35, 180, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 35, 150, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 35, 180, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(4, 35, 150, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(4, 35, 180, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(4, 35, 150, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(4, 35, 180, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(4, 35, 150, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(4, 35, 180, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(4, 35, 240, DATE_SUB(NOW(), INTERVAL 26 DAY)),
(4, 35, 150, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(4, 35, 300, DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 5: AC BADROOM (AC, 1500W) - night sessions 120-180 min
-- -------------------------------------------------------------------------
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(5, 1500, 180, DATE_SUB(NOW(), INTERVAL 26 DAY)),
(5, 1500, 150, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(5, 1500, 120, DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 6: BADROOM LIGHT TABLE LUMP (LIGHT, 9W) - reading/bedtime sessions
-- -------------------------------------------------------------------------
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 9, 45,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(6, 9, 30,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 9, 45,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, 9, 30,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(6, 9, 45,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(6, 9, 30,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(6, 9, 45,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(6, 9, 30,  DATE_SUB(NOW(), INTERVAL 19 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(6, 9, 45,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(6, 9, 30,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(6, 9, 90,  DATE_SUB(NOW(), INTERVAL 25 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(6, 9, 45,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(6, 9, 60,  DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 7: HALL FAN2 (FAN, 35W) - afternoon/evening sessions
-- -------------------------------------------------------------------------
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 35, 300, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(7, 35, 300, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(7, 35, 300, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(7, 35, 300, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(7, 35, 300, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(7, 35, 300, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(7, 35, 240, DATE_SUB(NOW(), INTERVAL 26 DAY)),
(7, 35, 120, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(7, 35, 180, DATE_SUB(NOW(), INTERVAL 28 DAY));

INSERT INTO device_analytics (device_id, power, duration, recorded_at) VALUES

-- -------------------------------------------------------------------------
-- Device 8: HALL LIGHT2 (LIGHT, 12W) - evening sessions
-- -------------------------------------------------------------------------
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(8, 12, 90,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(8, 12, 45,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 12, 30,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 12, 90,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, 12, 45,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(8, 12, 120, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(8, 12, 30,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(8, 12, 90,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(8, 12, 45,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(8, 12, 120, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(8, 12, 30,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(8, 12, 90,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(8, 12, 45,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(8, 12, 120, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(8, 12, 30,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(8, 12, 90,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(8, 12, 45,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(8, 12, 120, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(8, 12, 60,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(8, 12, 30,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(8, 12, 90,  DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 9: KITCHEN LIHGT (LIGHT, 9W) - morning/meal sessions
-- -------------------------------------------------------------------------
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 19 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 25 DAY)),
(9, 9, 30,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(9, 9, 60,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(9, 9, 45,  DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 10: HALL DOOR LOCK (LOCK, 5W) - brief lock/unlock events
-- -------------------------------------------------------------------------
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 10 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 11 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 12 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 13 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 14 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 15 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 16 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 17 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 18 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 19 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 19 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 20 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 21 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 22 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 23 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 24 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 25 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 25 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 26 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 27 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 28 DAY)),
(10, 5, 1,  DATE_SUB(NOW(), INTERVAL 28 DAY)),

-- -------------------------------------------------------------------------
-- Device 11: REFRIGRATOR (OTHER, 89W) - 480 min twice daily (always on)
-- -------------------------------------------------------------------------
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 0 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 14 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 15 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 16 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 17 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 18 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 19 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 20 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 21 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 22 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 23 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 24 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 25 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 26 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 26 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 27 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 28 DAY)),
(11, 89, 480, DATE_SUB(NOW(), INTERVAL 28 DAY));

-- =============================================================================
-- USER BEHAVIOR LOGS
-- Strong repeated patterns (6-8 occurrences each) for AI model training
-- Columns: user_id, device_id, action, source, hour_of_day, day_of_week, recorded_at
--
-- Patterns encoded:
--   AC ON       at hour 22  (device 5)
--   AC OFF      at hour 6   (device 5)
--   Bedroom Fan ON at hour 22 (device 4)
--   Kitchen Light ON  at hour 7  (device 9)
--   Kitchen Light OFF at hour 8  (device 9)
--   Hall Light ON  at hour 20 (device 1)
--   Hall Light OFF at hour 23 (device 1)
--   Hall Fan ON    at hour 10 (device 2)
--   Door Lock ON   at hour 22 (device 10)
-- =============================================================================

INSERT INTO user_behavior_logs (user_id, device_id, action, source, hour_of_day, day_of_week, recorded_at) VALUES

-- -------------------------------------------------------------------------
-- Pattern 1: AC ON at hour 22 (device 5) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 5, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 5, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 5, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 5, 'ON', 'VOICE', 22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 5, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 5, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 5, 'ON', 'VOICE', 22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 9 DAY)),  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 5, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 11 DAY)), DATE_SUB(NOW(), INTERVAL 11 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 2: AC OFF at hour 6 (device 5) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 5, 'OFF', 'APP',   6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 5, 'OFF', 'APP',   6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 5, 'OFF', 'APP',   6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 5, 'OFF', 'AUTO',  6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 5, 'OFF', 'APP',   6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 5, 'OFF', 'APP',   6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 5, 'OFF', 'AUTO',  6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 9 DAY)),  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 5, 'OFF', 'APP',   6, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 11 DAY)), DATE_SUB(NOW(), INTERVAL 11 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 3: Bedroom Fan ON at hour 22 (device 4) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 4, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 4, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 4, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 4, 'ON', 'VOICE', 22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 4 DAY)),  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 4, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 6 DAY)),  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 4, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 8 DAY)),  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 4, 'ON', 'VOICE', 22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 10 DAY)), DATE_SUB(NOW(), INTERVAL 10 DAY)),
(4, 4, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 12 DAY)), DATE_SUB(NOW(), INTERVAL 12 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 4: Kitchen Light ON at hour 7 (device 9) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 9, 'ON', 'APP',   7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 9, 'ON', 'APP',   7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 9, 'ON', 'APP',   7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 9, 'ON', 'AUTO',  7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 9, 'ON', 'APP',   7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 4 DAY)),  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 9, 'ON', 'APP',   7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 9, 'ON', 'AUTO',  7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 6 DAY)),  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 9, 'ON', 'APP',   7, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 5: Kitchen Light OFF at hour 8 (device 9) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 9, 'OFF', 'APP',   8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 9, 'OFF', 'APP',   8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 9, 'OFF', 'APP',   8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 9, 'OFF', 'AUTO',  8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 9, 'OFF', 'APP',   8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 4 DAY)),  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 9, 'OFF', 'APP',   8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 9, 'OFF', 'AUTO',  8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 6 DAY)),  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 9, 'OFF', 'APP',   8, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 6: Hall Light ON at hour 20 (device 1) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 1, 'ON', 'APP',   20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 1, 'ON', 'APP',   20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 1, 'ON', 'APP',   20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 1, 'ON', 'VOICE', 20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 1, 'ON', 'APP',   20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 1, 'ON', 'APP',   20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 1, 'ON', 'VOICE', 20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 9 DAY)),  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 1, 'ON', 'APP',   20, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 11 DAY)), DATE_SUB(NOW(), INTERVAL 11 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 7: Hall Light OFF at hour 23 (device 1) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 1, 'OFF', 'APP',   23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 1, 'OFF', 'APP',   23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 1, 'OFF', 'APP',   23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 1, 'OFF', 'AUTO',  23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 1, 'OFF', 'APP',   23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 1, 'OFF', 'APP',   23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 1, 'OFF', 'AUTO',  23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 9 DAY)),  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 1, 'OFF', 'APP',   23, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 11 DAY)), DATE_SUB(NOW(), INTERVAL 11 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 8: Hall Fan ON at hour 10 (device 2) - 7 occurrences
-- -------------------------------------------------------------------------
(4, 2, 'ON', 'APP',   10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 2, 'ON', 'APP',   10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 2, 'ON', 'APP',   10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 2, 'ON', 'VOICE', 10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 4 DAY)),  DATE_SUB(NOW(), INTERVAL 4 DAY)),
(4, 2, 'ON', 'APP',   10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 6 DAY)),  DATE_SUB(NOW(), INTERVAL 6 DAY)),
(4, 2, 'ON', 'APP',   10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 8 DAY)),  DATE_SUB(NOW(), INTERVAL 8 DAY)),
(4, 2, 'ON', 'VOICE', 10, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 10 DAY)), DATE_SUB(NOW(), INTERVAL 10 DAY)),

-- -------------------------------------------------------------------------
-- Pattern 9: Door Lock ON at hour 22 (device 10) - 8 occurrences
-- -------------------------------------------------------------------------
(4, 10, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 0 DAY)),  DATE_SUB(NOW(), INTERVAL 0 DAY)),
(4, 10, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 1 DAY)),  DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 10, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 2 DAY)),  DATE_SUB(NOW(), INTERVAL 2 DAY)),
(4, 10, 'ON', 'AUTO',  22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 3 DAY)),  DATE_SUB(NOW(), INTERVAL 3 DAY)),
(4, 10, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 5 DAY)),  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 10, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 7 DAY)),  DATE_SUB(NOW(), INTERVAL 7 DAY)),
(4, 10, 'ON', 'AUTO',  22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 9 DAY)),  DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 10, 'ON', 'APP',   22, DAYOFWEEK(DATE_SUB(NOW(), INTERVAL 11 DAY)), DATE_SUB(NOW(), INTERVAL 11 DAY));
