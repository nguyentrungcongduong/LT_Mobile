package com.gymapp.modules.user.repository;

import com.gymapp.modules.user.entity.PtProfile;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PtProfileSpecification {

    public static Specification<PtProfile> getFilterSpecification(String specialization, BigDecimal minRating, BigDecimal maxPrice, Boolean isApproved) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (specialization != null && !specialization.trim().isEmpty()) {
                // Convert PostgreSQL text[] to string for LIKE search
                jakarta.persistence.criteria.Expression<String> arrayToString = cb.function("array_to_string", String.class, root.get("specializations"), cb.literal(","));
                predicates.add(cb.like(cb.lower(arrayToString), "%" + specialization.toLowerCase() + "%"));
            }

            if (minRating != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ratingAvg"), minRating));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerSession"), maxPrice));
            }

            if (isApproved != null) {
                predicates.add(cb.equal(root.get("isApproved"), isApproved));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
