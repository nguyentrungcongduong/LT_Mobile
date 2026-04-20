package com.gymapp.modules.user.repository;

import com.gymapp.modules.user.entity.CheckinLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CheckinLogRepository extends JpaRepository<CheckinLog, UUID> {

        /** Kiểm tra user đã check-in hôm nay chưa */
        @Query("SELECT c FROM CheckinLog c WHERE c.user.id = :userId AND c.checkinDate = :date")
        Optional<CheckinLog> findByUserIdAndCheckinDate(
                        @Param("userId") UUID userId,

                        @Param("date") LocalDate date);

        /** Admin: lấy toàn bộ lịch sử check-in */
        @Query("""
                            SELECT c FROM CheckinLog c
                            JOIN FETCH c.user
                            LEFT JOIN FETCH c.branch
                            ORDER BY c.checkinTime DESC
                        """)
        Page<CheckinLog> findAllOrderByCheckinTimeDesc(Pageable pageable);

        /** Admin: lọc theo ngày */
        @Query("""
                            SELECT c FROM CheckinLog c
                            JOIN FETCH c.user
                            LEFT JOIN FETCH c.branch
                            WHERE c.checkinDate = :date
                            ORDER BY c.checkinTime DESC
                        """)
        Page<CheckinLog> findByCheckinDate(
                        @Param("date") LocalDate date,
                        Pageable pageable);

        /** Admin: lọc theo chi nhánh */
        @Query("""
                            SELECT c FROM CheckinLog c
                            JOIN FETCH c.user
                            LEFT JOIN FETCH c.branch
                            WHERE c.branch.id = :branchId
                            ORDER BY c.checkinTime DESC
                        """)
        Page<CheckinLog> findByBranchId(
                        @Param("branchId") UUID branchId,
                        Pageable pageable);

        /** Admin: lọc theo user */
        @Query("""
                            SELECT c FROM CheckinLog c
                            JOIN FETCH c.user
                            LEFT JOIN FETCH c.branch
                            WHERE c.user.id = :userId
                            ORDER BY c.checkinTime DESC
                        """)
        Page<CheckinLog> findByUserId(
                        @Param("userId") UUID userId,
                        Pageable pageable);

        @Query("""
                            SELECT c FROM CheckinLog c
                            JOIN FETCH c.user
                            LEFT JOIN FETCH c.branch
                            ORDER BY c.checkinTime DESC
                        """)
        List<CheckinLog> findAllForExport();
}
