-- ═══════════════════════════════════════════════════════════
-- V8: Seed data for Exercises and Workout Plans
-- ═══════════════════════════════════════════════════════════

DO $$
DECLARE
    admin_id UUID;
    
    -- Exercises
    pushup_id UUID := gen_random_uuid();
    squat_id UUID := gen_random_uuid();
    plank_id UUID := gen_random_uuid();
    lunge_id UUID := gen_random_uuid();
    crunch_id UUID := gen_random_uuid();
    pullup_id UUID := gen_random_uuid();
    dl_id UUID := gen_random_uuid();
    bp_id UUID := gen_random_uuid();
    row_id UUID := gen_random_uuid();
    ohp_id UUID := gen_random_uuid();

    -- Beginner Plans
    plan_beg1_id UUID := gen_random_uuid();
    plan_beg2_id UUID := gen_random_uuid();
    plan_beg3_id UUID := gen_random_uuid();

    -- Intermediate Plans
    plan_int1_id UUID := gen_random_uuid();
    plan_int2_id UUID := gen_random_uuid();

    -- Advanced Plans
    plan_adv1_id UUID := gen_random_uuid();

BEGIN
    -- Find an ADMIN user to be 'created_by'
    SELECT id INTO admin_id FROM users WHERE role = 'ADMIN' LIMIT 1;
    
    -- If no admin exists, insert one
    IF admin_id IS NULL THEN
        admin_id := gen_random_uuid();
        INSERT INTO users (id, email, password_hash, full_name, role, is_active) 
        VALUES (admin_id, 'admin_seeder@fitnessapp.com', '$2a$10$dummyhashformigration123', 'System Seeder', 'ADMIN', true);
    END IF;

    -- Seed 10 sample exercises
    INSERT INTO exercises (id, name, muscle_group, description, created_by, is_public) VALUES 
        (pushup_id, 'Knee Push Ups', 'Chest', 'A beginner-friendly push up variation.', admin_id, true),
        (squat_id, 'Bodyweight Squats', 'Legs', 'A fundamental lower body movement.', admin_id, true),
        (plank_id, 'Elbow Plank', 'Core', 'For building core endurance.', admin_id, true),
        (lunge_id, 'Walking Lunges', 'Legs', 'Enhance balance and leg strength.', admin_id, true),
        (crunch_id, 'Basic Crunches', 'Core', 'Classic abdominal exercise.', admin_id, true),
        (pullup_id, 'Pull Ups', 'Back', 'Advanced upper body pulling exercise.', admin_id, true),
        (dl_id, 'Barbell Deadlift', 'Full Body', 'Heavy compound lift.', admin_id, true),
        (bp_id, 'Barbell Bench Press', 'Chest', 'Standard chest building exercise.', admin_id, true),
        (row_id, 'Dumbbell Rows', 'Back', 'Builds lat thickness.', admin_id, true),
        (ohp_id, 'Overhead Press', 'Shoulders', 'Essential shoulder compound lift.', admin_id, true);

    -- ════════════════════════════════════════════════════════
    -- 3 BEGINNER PLANS
    -- ════════════════════════════════════════════════════════
    INSERT INTO workout_plans (id, name, description, plan_type, target_level, created_by, is_active) 
    VALUES (plan_beg1_id, 'Beginner Full Body Basics', 'A full body workout suitable for true beginners.', 'USER_CUSTOM', 'BEGINNER', admin_id, true);
    
    INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, order_index) VALUES 
        (plan_beg1_id, squat_id, 3, 10, 0),
        (plan_beg1_id, pushup_id, 3, 8, 1),
        (plan_beg1_id, plank_id, 3, 30, 2);

    INSERT INTO workout_plans (id, name, description, plan_type, target_level, created_by, is_active) 
    VALUES (plan_beg2_id, 'Beginner Core & Cardio', 'Focus on building a strong core foundation.', 'USER_CUSTOM', 'BEGINNER', admin_id, true);
    
    INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, order_index) VALUES 
        (plan_beg2_id, crunch_id, 3, 15, 0),
        (plan_beg2_id, plank_id, 3, 40, 1),
        (plan_beg2_id, lunge_id, 3, 12, 2);

    INSERT INTO workout_plans (id, name, description, plan_type, target_level, created_by, is_active) 
    VALUES (plan_beg3_id, 'Beginner Legs', 'Build lower body strength without weights.', 'USER_CUSTOM', 'BEGINNER', admin_id, true);
    
    INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, order_index) VALUES 
        (plan_beg3_id, squat_id, 4, 12, 0),
        (plan_beg3_id, lunge_id, 4, 10, 1);

    -- ════════════════════════════════════════════════════════
    -- 2 INTERMEDIATE PLANS
    -- ════════════════════════════════════════════════════════
    INSERT INTO workout_plans (id, name, description, plan_type, target_level, created_by, is_active) 
    VALUES (plan_int1_id, 'Intermediate Upper Body', 'Build upper body mass and strength.', 'USER_CUSTOM', 'INTERMEDIATE', admin_id, true);
    
    INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, order_index) VALUES 
        (plan_int1_id, bp_id, 4, 10, 0),
        (plan_int1_id, row_id, 4, 10, 1),
        (plan_int1_id, ohp_id, 3, 10, 2);

    INSERT INTO workout_plans (id, name, description, plan_type, target_level, created_by, is_active) 
    VALUES (plan_int2_id, 'Intermediate Lower & Core', 'A challenging mix for lower body and core stability.', 'USER_CUSTOM', 'INTERMEDIATE', admin_id, true);
    
    INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, order_index) VALUES 
        (plan_int2_id, dl_id, 4, 8, 0),
        (plan_int2_id, squat_id, 4, 10, 1),
        (plan_int2_id, plank_id, 4, 60, 2);

    -- ════════════════════════════════════════════════════════
    -- 1 ADVANCED PLAN
    -- ════════════════════════════════════════════════════════
    INSERT INTO workout_plans (id, name, description, plan_type, target_level, created_by, is_active) 
    VALUES (plan_adv1_id, 'Advanced Power & Strength', 'Heavy compound lifts for advanced users.', 'USER_CUSTOM', 'ADVANCED', admin_id, true);
    
    INSERT INTO plan_exercises (plan_id, exercise_id, sets, reps, order_index) VALUES 
        (plan_adv1_id, dl_id, 5, 5, 0),
        (plan_adv1_id, bp_id, 5, 5, 1),
        (plan_adv1_id, pullup_id, 4, 12, 2),
        (plan_adv1_id, ohp_id, 4, 8, 3);

END $$;
