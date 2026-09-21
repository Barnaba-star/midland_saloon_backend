package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.ServiceAndStoreReport;
import com.midland.saloon.Saloon.Projection.CommissionTotalProjection;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import com.midland.saloon.Saloon.Projection.StoreReportSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface ServiceAndStoreReportRepository extends JpaRepository<ServiceAndStoreReport, String> {
    @Query("""
    SELECT
        sas.storeOpen.uid AS storeOpenUid,

        sas.storeOpen.openStoreCode AS openStoreCode,

        sas.storeOpen.createdAt AS openedDate,

        sas.storeOpen.updatedAt AS closedDate,

        sas.storeOpen.status AS status,

        sas.storeOpen.store.buyingPrice AS buyingPrice,

        sas.storeOpen.store.saloonServiceEntity.serviceName AS serviceName,
        
        COALESCE(SUM(sas.sharedAmount), 0)
            AS sharedAmount,

        COALESCE(SUM(sas.saloonReports.traAmount), 0)
            AS traAmount,

        COALESCE(SUM(sas.saloonReports.emergencyAmount), 0)
            AS emergencyAmount,

        COALESCE(SUM(sas.saloonReports.staffAmount), 0)
            AS staffAmount,

        COALESCE(SUM(sas.saloonReports.othersAmount), 0)
            AS othersAmount,

        COALESCE(SUM(sas.saloonReports.ownerAmount), 0)
            AS ownerAmount,
        COALESCE(SUM(sas.saloonReports.rentAmount), 0)
            AS rentAmount,
        COALESCE(SUM(sas.saloonReports.loanAmount), 0)
            AS loanAmount,
        COALESCE(SUM(sas.saloonReports.waterAmount), 0)
            AS waterAmount,
        COALESCE(SUM(sas.saloonReports.lukuAmount), 0)
            AS lukuAmount,
        COALESCE(SUM(sas.saloonReports.stockPurchaseAmount), 0)
            AS stockPurchaseAmount,
        (
            COALESCE(SUM(sas.saloonReports.traAmount), 0)
            + COALESCE(SUM(sas.saloonReports.emergencyAmount), 0)
            + COALESCE(SUM(sas.saloonReports.staffAmount), 0)
            + COALESCE(SUM(sas.saloonReports.othersAmount), 0)
            + COALESCE(SUM(sas.saloonReports.ownerAmount), 0)
            + COALESCE(SUM(sas.saloonReports.rentAmount), 0)
            + COALESCE(SUM(sas.saloonReports.loanAmount), 0)
            + COALESCE(SUM(sas.saloonReports.waterAmount), 0)
            + COALESCE(SUM(sas.saloonReports.lukuAmount), 0)
            + COALESCE(SUM(sas.saloonReports.stockPurchaseAmount), 0)
            + COALESCE(SUM(sas.saloonReports.maintenanceAmount), 0)
        ) AS totalPrice,

        COALESCE(SUM(sas.saloonReports.maintenanceAmount), 0)
            AS maintenanceAmount,

        COUNT(sas.uid) AS reportCount

    FROM ServiceAndStoreReport sas

    WHERE sas.branchUid = :branchUID

    AND sas.storeOpen.status = 'CLOSED'

    AND sas.storeOpen.updatedAt >= :startDate

    AND sas.storeOpen.updatedAt < :endDate

    GROUP BY
        sas.storeOpen.uid,
        sas.storeOpen.openStoreCode,
        sas.storeOpen.createdAt,
        sas.storeOpen.updatedAt,
        sas.storeOpen.status,
        sas.storeOpen.store.buyingPrice,
        sas.storeOpen.store.saloonServiceEntity.serviceName

    ORDER BY sas.storeOpen.updatedAt DESC
""")
    Page<StoreReportSummaryProjection> findStoreReportSummaryPage(
            Pageable pageable,
            @Param("branchUID") String branchUID,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


}
