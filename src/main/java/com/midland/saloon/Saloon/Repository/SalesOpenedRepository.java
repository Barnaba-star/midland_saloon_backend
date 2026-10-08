package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.SalesOpened;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesOpenedRepository extends JpaRepository<SalesOpened, String> {
    // Every bill still waiting for payment, whatever day it was opened - a bill
    // left from yesterday has to show here to be paid (it was counted on the
    // dashboard but never listed). Oldest first, cancelled ones out.
    @Query("SELECT s FROM SalesOpened s WHERE s.branchUid = :branchUID AND s.paymentStatus = 'PENDING' " +
           "AND (s.isActive IS NULL OR s.isActive = true) ORDER BY s.createdAt, s.salesCode")
    List<SalesOpened> salesOpenedList(@Param("branchUID") String branchUID);
    @Query("""
    SELECT s
    FROM SalesOpened s
    WHERE s.branchUid = :branchUID
      AND s.createdAt >= :startDate
      AND s.createdAt < :endDate
      AND (s.isActive IS NULL OR s.isActive = true)
""")
    List<SalesOpened> salesOpenedListByStatus(
            @Param("branchUID") String branchUID,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Sales still open and unpaid. Not scoped to today on purpose - a bill
    // left from last week is exactly the one worth chasing.
    @Query("SELECT COUNT(s) FROM SalesOpened s WHERE s.branchUid = :branchUID AND s.paymentStatus = 'PENDING' " +
           "AND (s.isActive IS NULL OR s.isActive = true)")
    long countPendingBills(@Param("branchUID") String branchUID);

    @Query("""
            SELECT COALESCE(SUM(COALESCE(s.bill,0) - COALESCE(s.paidAmount,0)), 0)
            FROM SalesOpened s
            WHERE s.branchUid = :branchUID AND s.paymentStatus = 'PENDING'
              AND (s.isActive IS NULL OR s.isActive = true)
            """)
    long pendingBillsAmount(@Param("branchUID") String branchUID);

    /** One cashier's takings in [from, to): method, amount, bills - what their cash-up expects. */
    @Query("SELECT LOWER(s.paymentMethod), COALESCE(SUM(s.paidAmount), 0), COUNT(s) FROM SalesOpened s " +
           "WHERE s.branchUid = :branchUID AND s.paidBy = :email AND s.isActive = true AND s.paymentStatus = 'PAID' " +
           "AND s.paidAt >= :from AND s.paidAt < :to GROUP BY LOWER(s.paymentMethod)")
    List<Object[]> takingsOf(@Param("branchUID") String branchUID, @Param("email") String email,
                             @Param("from") java.time.LocalDateTime from, @Param("to") java.time.LocalDateTime to);
}
