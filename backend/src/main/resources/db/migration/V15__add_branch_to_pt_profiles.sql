-- Add branch_id to pt_profiles so PTs can be assigned to a specific branch
ALTER TABLE pt_profiles
    ADD COLUMN branch_id UUID REFERENCES branches(id) ON DELETE SET NULL;
