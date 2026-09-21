package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.StaffCommissions;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffCommissionsRepository extends JpaRepository<StaffCommissions, String> {


    @Query("""
                SELECT s
                FROM StaffCommissions s
                WHERE s.saloonStaff.uid = :staffUid
                  AND s.branchUid = :branchUid
                  AND s.createdAt = :date
            """)
    Optional<StaffCommissions> findCommission(@Param("staffUid") String staffUid, @Param("branchUid") String branchUid, @Param("date") LocalDate date);

    @Query("""
    SELECT
        s.uid AS uid,
        s.totalAmount AS totalAmount,
        s.payedAmount AS paidAmount,
        s.remainingAmount AS remainingAmount,
        s.createdAt AS date,
        s.saloonStaff.firstName AS firstName,
        s.saloonStaff.middleName AS middleName,
        s.saloonStaff.lastName AS lastName,
        s.saloonStaff.saloonCategory AS saloonCategory,
        s.weekDate AS weekDate,

        CASE
            WHEN s.remainingAmount = 0
                THEN 'PAID'
            WHEN s.payedAmount = 0
                THEN 'NOT PAID'
            ELSE 'PARTIAL'
        END AS status

    FROM StaffCommissions s

    WHERE s.branchUid = :branchUid
      AND s.createdAt >= :startDate
      AND s.createdAt <= :endDate

    ORDER BY s.saloonStaff.firstName ASC
""")
    Page<SaloonProjection> findStaffCommissionPage(
            Pageable pageable,
            @Param("branchUid") String branchUid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate

    );


    @Query("""
    SELECT s
    FROM StaffCommissions s

    WHERE s.branchUid = :branchUid
      AND s.createdAt >= :startDate
      AND s.createdAt <= :endDate
      AND s.saloonStaff.firstName = :firstName
      AND s.saloonStaff.middleName = :middleName
      AND s.saloonStaff.lastName = :lastName

    ORDER BY s.createdAt DESC
""")
    List<StaffCommissions> findStaffCommissionListForPayment(
            @Param("branchUid") String branchUid,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("firstName") String firstName,
            @Param("middleName") String middleName,
            @Param("lastName") String lastName
    );

}


