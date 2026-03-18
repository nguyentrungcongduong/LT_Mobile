-- ═══════════════════════════════════════════════════════════
-- V1: Create all ENUMs
-- ═══════════════════════════════════════════════════════════

CREATE TYPE user_role AS ENUM ('USER', 'PT', 'ADMIN');
CREATE TYPE membership_status AS ENUM ('PENDING', 'ACTIVE', 'EXPIRED', 'FROZEN', 'CANCELLED');
CREATE TYPE plan_type AS ENUM ('SINGLE', 'ALL');
CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED');
CREATE TYPE cancel_by_type AS ENUM ('USER', 'PT');
CREATE TYPE payment_type AS ENUM ('PT_SESSION', 'MEMBERSHIP');
CREATE TYPE payment_status AS ENUM ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED');
CREATE TYPE payment_provider AS ENUM ('VNPAY', 'MOMO');
CREATE TYPE refund_status AS ENUM ('PENDING', 'PROCESSING', 'PROCESSED', 'FAILED');
CREATE TYPE earning_status AS ENUM ('PENDING', 'AVAILABLE', 'WITHDRAWN');
CREATE TYPE wp_type AS ENUM ('PT_ASSIGNED', 'USER_CUSTOM');
CREATE TYPE notif_type AS ENUM (
    'BOOKING_CONFIRMED',
    'BOOKING_CANCELLED',
    'SESSION_REMINDER',
    'MEMBERSHIP_EXPIRING',
    'MEMBERSHIP_EXPIRED',
    'PAYMENT_SUCCESS',
    'PAYMENT_FAILED',
    'SYSTEM'
);
