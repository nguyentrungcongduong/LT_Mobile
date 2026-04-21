package com.gymapp.modules.payment.repository;

import com.gymapp.modules.payment.entity.Refund;
import com.gymapp.modules.payment.enums.RefundStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Refund> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = { "payment" })
    List<Refund> findAllByStatus(RefundStatus status);
}
