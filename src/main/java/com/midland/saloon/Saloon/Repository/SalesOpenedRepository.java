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
    @Query("SELECT s FROM SalesOpened s WHERE s.branchUid = :branchUID AND s.paymentStatus='PENDING' AND s.createdAt=:date ")
    List<SalesOpened> salesOpenedList(String branchUID, LocalDate date);
    @Query("""
    SELECT s
    FROM SalesOpened s
    WHERE s.branchUid = :branchUID
      AND s.createdAt >= :startDate
      AND s.createdAt < :endDate
""")
    List<SalesOpened> salesOpenedListByStatus(
            @Param("branchUID") String branchUID,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


}
