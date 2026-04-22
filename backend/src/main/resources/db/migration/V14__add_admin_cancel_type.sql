-- Add 'ADMIN' to cancel_by_type to support admin-initiated cancellations
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'ADMIN'
                   AND enumtypid = (SELECT oid FROM pg_type WHERE typname = 'cancel_by_type')) THEN
        ALTER TYPE cancel_by_type ADD VALUE 'ADMIN';
    END IF;
END$$;
