-- ============================================================
-- V13: Create pt_reviews table for PT star ratings
-- ============================================================

CREATE TABLE pt_reviews (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pt_id       UUID NOT NULL,
    user_id     UUID NOT NULL,
    booking_id  UUID,
    rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_pt_reviews_pt     FOREIGN KEY (pt_id)      REFERENCES pt_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_pt_reviews_user   FOREIGN KEY (user_id)    REFERENCES users(id)       ON DELETE CASCADE,
    CONSTRAINT fk_pt_reviews_bk     FOREIGN KEY (booking_id) REFERENCES bookings(id)    ON DELETE SET NULL,
    CONSTRAINT uq_pt_review_user_pt UNIQUE (user_id, pt_id)
);

CREATE INDEX idx_pt_reviews_pt_id   ON pt_reviews(pt_id);
CREATE INDEX idx_pt_reviews_user_id ON pt_reviews(user_id);
