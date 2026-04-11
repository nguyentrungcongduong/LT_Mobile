-- Add target_level column to workout_plans 
ALTER TABLE workout_plans 
ADD COLUMN target_level VARCHAR(20);
