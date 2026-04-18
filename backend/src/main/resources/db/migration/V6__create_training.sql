-- ═══════════════════════════════════════════════════════════
-- V6: Create Training tables
-- ═══════════════════════════════════════════════════════════

CREATE TABLE exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    muscle_group VARCHAR(100) NULL,
    description TEXT NULL,
    video_url TEXT NULL,
    thumbnail_url TEXT NULL,
    created_by UUID NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_exercises_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE workout_plans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT NULL,
    created_by UUID NOT NULL,
    plan_type wp_type NOT NULL,
    assigned_to UUID NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_workout_plans_created_by FOREIGN KEY (created_by) REFERENCES users(id),
    CONSTRAINT fk_workout_plans_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE plan_exercises (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    plan_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    sets INTEGER NOT NULL DEFAULT 3,
    reps INTEGER NOT NULL DEFAULT 10,
    rest_seconds INTEGER NOT NULL DEFAULT 60,
    order_index INTEGER NOT NULL DEFAULT 0,
    notes TEXT NULL,
    CONSTRAINT fk_plan_exercises_plan_id FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_exercises_exercise_id FOREIGN KEY (exercise_id) REFERENCES exercises(id),
    CONSTRAINT uq_plan_exercise_order UNIQUE (plan_id, order_index)
);

CREATE TABLE workout_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plan_id UUID NULL,
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    duration_minutes INTEGER NULL,
    notes TEXT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_workout_logs_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_workout_logs_plan_id FOREIGN KEY (plan_id) REFERENCES workout_plans(id) ON DELETE SET NULL
);

CREATE INDEX idx_workout_logs_user_date ON workout_logs(user_id, log_date);
