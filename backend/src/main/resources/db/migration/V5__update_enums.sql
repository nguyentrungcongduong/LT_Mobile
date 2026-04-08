-- ═══════════════════════════════════════════════════════════
-- V5: Update ENUMs to include SYSTEM type
-- ═══════════════════════════════════════════════════════════

-- Add 'SYSTEM' to cancel_by_type to support auto-cancellations
ALTER TYPE cancel_by_type ADD VALUE 'SYSTEM';

-- Rename 'PT_SESSION' to 'BOOKING' for consistency with current business naming
ALTER TYPE payment_type RENAME VALUE 'PT_SESSION' TO 'BOOKING';

-- Add 'SYSTEM' to payment_type for system-level transactions
ALTER TYPE payment_type ADD VALUE 'SYSTEM';

-- Add 'CANCELLED' to earning_status for cancelled bookings
ALTER TYPE earning_status ADD VALUE 'CANCELLED';