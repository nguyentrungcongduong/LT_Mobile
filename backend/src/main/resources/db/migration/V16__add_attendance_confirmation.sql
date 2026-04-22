-- Add new statuses to booking_status enum
ALTER TYPE booking_status ADD VALUE IF NOT EXISTS 'AWAITING_CONFIRMATION';
ALTER TYPE booking_status ADD VALUE IF NOT EXISTS 'NO_SHOW';

-- Add pt_confirmed_at timestamp to bookings
ALTER TABLE bookings
    ADD COLUMN IF NOT EXISTS pt_confirmed_at TIMESTAMP WITH TIME ZONE;
