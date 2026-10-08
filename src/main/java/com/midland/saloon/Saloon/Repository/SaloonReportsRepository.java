package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.SaloonReports;
import com.midland.saloon.Saloon.Projection.RevenueTrendProjection;
import com.midland.saloon.Saloon.Projection.StaffEarningsProjection;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import com.midland.saloon.Saloon.Projection.SaloonServiceRevenueProjection;
import com.midland.saloon.Utils.Responses.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SaloonReportsRepository extends JpaRepository<SaloonReports, String> {
    @Query("""
            SELECT r FROM SaloonReports r WHERE r.uid=:saloonReportUID AND r.branchUid=:branchUID
            """)
    Optional<SaloonReports> findSaloonReportByUID(String saloonReportUID, String branchUID);

    @Query("""
            SELECT r.uid as uid, r.traAmount as traAmount, r.ownerAmount as ownerAmount, r.staffAmount as staffAmount,
            r.emergencyAmount as emergencyAmount, r.othersAmount as othersAmount, r.maintenanceAmount as maintenanceAmount,
            sls.paymentMethod as paymentMethod, r.rentAmount as rentAmount,
           r.loanAmount as loanAmount,
           r.waterAmount as waterAmount,
           r.lukuAmount as lukuAmount,
           r.stockPurchaseAmount as stockPurchaseAmount,
            stf.firstName as firstName, stf.middleName as middleName, stf.lastName as lastName,
            ssv.serviceName as serviceName, ssv.serviceCode as serviceCode, ssv.price as price
            FROM SaloonReports r LEFT JOIN r.saloonStaff stf LEFT JOIN r.saloonServiceEntity ssv LEFT JOIN r.saloonSales sls
            WHERE r.branchUid=:branchUID AND r.isActive=true
            """)
    List<SaloonReports> findSaloonReportsList(String branchUID);

    @Query("""
            SELECT r.uid as uid, r.traAmount as traAmount, r.ownerAmount as ownerAmount, r.staffAmount as staffAmount,
            r.emergencyAmount as emergencyAmount, r.othersAmount as othersAmount, r.maintenanceAmount as maintenanceAmount,
            sls.paymentMethod as paymentMethod, r.rentAmount as rentAmount,
           r.loanAmount as loanAmount,
           r.waterAmount as waterAmount,
           r.lukuAmount as lukuAmount,
           r.stockPurchaseAmount as stockPurchaseAmount,
            stf.firstName as firstName, stf.middleName as middleName, stf.lastName as lastName,
            ssv.serviceName as serviceName, ssv.serviceCode as serviceCode, ssv.price as price
            FROM SaloonReports r LEFT JOIN r.saloonStaff stf LEFT JOIN r.saloonServiceEntity ssv LEFT JOIN r.saloonSales sls
            WHERE r.branchUid=:branchUID AND r.isActive=true AND r.createdAt = :date
            """)
    Page<SaloonProjection> findSaloonReportsPage(Pageable pageable, String branchUID, LocalDate date);

    @Query("""
            SELECT  SUM(r.traAmount) as traAmount, SUM(r.ownerAmount) as ownerAmount, SUM(r.staffAmount) as staffAmount,
            SUM(r.emergencyAmount) as emergencyAmount, SUM(r.othersAmount) as othersAmount, SUM(r.maintenanceAmount) as maintenanceAmount,
            SUM(r.rentAmount) as rentAmount, SUM(r.loanAmount) as loanAmount, SUM(r.lukuAmount) as lukuAmount, SUM(r.waterAmount) as waterAmount,
            SUM(r.stockPurchaseAmount) as stockPurchaseAmount
            FROM SaloonReports r WHERE r.branchUid=:branchUID AND r.isActive=true AND r.createdAt = :date
            """)
    Optional<SaloonProjection> findSaloonRevenueReport(String branchUID, LocalDate date);

    @Query("""
        SELECT
            SUM(COALESCE(r.traAmount, 0)) as traAmount,
            SUM(COALESCE(r.ownerAmount, 0)) as ownerAmount,
            SUM(COALESCE(r.staffAmount, 0)) as staffAmount,
            SUM(COALESCE(r.emergencyAmount, 0)) as emergencyAmount,
            SUM(COALESCE(r.othersAmount, 0)) as othersAmount,
            SUM(COALESCE(r.loanAmount, 0)) as loanAmount,
        SUM(COALESCE(r.rentAmount, 0)) as rentAmount,
        SUM(COALESCE(r.waterAmount, 0)) as waterAmount,
        SUM(COALESCE(r.lukuAmount, 0)) as lukuAmount,
        SUM(COALESCE(r.stockPurchaseAmount, 0)) as stockPurchaseAmount,
            SUM(COALESCE(r.maintenanceAmount, 0)) as maintenanceAmount
        FROM SaloonReports r
        WHERE r.branchUid = :branchUID
          AND r.isActive = true
          AND r.createdAt >= :startDate
          AND r.createdAt < :endDate
        """)
    Optional<SaloonProjection> findCurrentSaloonRevenueReport(
            String branchUID,
            LocalDate startDate,
            LocalDate endDate
    );


    @Query("""
    SELECT
        ssv.uid as serviceUID,
        ssv.serviceName as serviceName,
        ssv.serviceCode as serviceCode,

        SUM(COALESCE(r.traAmount, 0)) as traAmount,
        SUM(COALESCE(r.ownerAmount, 0)) as ownerAmount,
        SUM(COALESCE(r.staffAmount, 0)) as staffAmount,
        SUM(COALESCE(r.emergencyAmount, 0)) as emergencyAmount,
        SUM(COALESCE(r.othersAmount, 0)) as othersAmount,
        SUM(COALESCE(r.loanAmount, 0)) as loanAmount,
        SUM(COALESCE(r.rentAmount, 0)) as rentAmount,
        SUM(COALESCE(r.waterAmount, 0)) as waterAmount,
        SUM(COALESCE(r.lukuAmount, 0)) as lukuAmount,
        SUM(COALESCE(r.stockPurchaseAmount, 0)) as stockPurchaseAmount,
        SUM(COALESCE(r.maintenanceAmount, 0)) as maintenanceAmount,

        SUM(
            COALESCE(r.traAmount, 0) +
            COALESCE(r.ownerAmount, 0) +
            COALESCE(r.staffAmount, 0) +
            COALESCE(r.emergencyAmount, 0) +
            COALESCE(r.othersAmount, 0) +
            COALESCE(r.rentAmount, 0) +
            COALESCE(r.loanAmount, 0) +
            COALESCE(r.waterAmount, 0) +
            COALESCE(r.lukuAmount, 0) +
            COALESCE(r.stockPurchaseAmount, 0) +
            COALESCE(r.maintenanceAmount, 0)
        ) as totalAmount

    FROM SaloonReports r

    LEFT JOIN r.saloonServiceEntity ssv

    WHERE r.branchUid = :branchUID
      AND r.isActive = true
      AND r.createdAt = :date

    GROUP BY
        ssv.uid,
        ssv.serviceName,
        ssv.serviceCode

    ORDER BY
        ssv.serviceName ASC
    """)
    List<SaloonServiceRevenueProjection> findSaloonRevenueByService(
            String branchUID,
            LocalDate date
    );

    @Query("""
    SELECT r.uid as uid,
           r.traAmount as traAmount,
           r.ownerAmount as ownerAmount,
           r.staffAmount as staffAmount,
           r.emergencyAmount as emergencyAmount,
           r.othersAmount as othersAmount,
           r.maintenanceAmount as maintenanceAmount,
           r.rentAmount as rentAmount,
           r.loanAmount as loanAmount,
           r.waterAmount as waterAmount,
           r.lukuAmount as lukuAmount,
           r.stockPurchaseAmount as stockPurchaseAmount,
           sls.paymentMethod as paymentMethod,
           stf.firstName as firstName,
           stf.middleName as middleName,
           stf.lastName as lastName,
           ssv.serviceName as serviceName,
           ssv.serviceCode as serviceCode,
           ssv.price as price
    FROM SaloonReports r
    LEFT JOIN r.saloonStaff stf
    LEFT JOIN r.saloonServiceEntity ssv
    LEFT JOIN r.saloonSales sls
    WHERE r.branchUid = :branchUID
      AND r.isActive = true
      AND r.createdAt >= :startDate
      AND r.createdAt < :endDate
    """)
    Page<SaloonProjection> findCurrentSaloonReportsPage(
            Pageable pageable,
            String branchUID,
            LocalDate startDate,
            LocalDate endDate
    );

    @Query("""
    SELECT
        ssv.uid as serviceUID,
        ssv.serviceName as serviceName,
        ssv.serviceCode as serviceCode,

        SUM(COALESCE(r.traAmount, 0)) as traAmount,
        SUM(COALESCE(r.ownerAmount, 0)) as ownerAmount,
        SUM(COALESCE(r.staffAmount, 0)) as staffAmount,
        SUM(COALESCE(r.emergencyAmount, 0)) as emergencyAmount,
        SUM(COALESCE(r.othersAmount, 0)) as othersAmount,
        SUM(COALESCE(r.loanAmount, 0)) as loanAmount,
        SUM(COALESCE(r.rentAmount, 0)) as rentAmount,
        SUM(COALESCE(r.waterAmount, 0)) as waterAmount,
        SUM(COALESCE(r.lukuAmount, 0)) as lukuAmount,
        SUM(COALESCE(r.stockPurchaseAmount, 0)) as stockPurchaseAmount,
        SUM(COALESCE(r.maintenanceAmount, 0)) as maintenanceAmount,

        SUM(
            COALESCE(r.traAmount, 0) +
            COALESCE(r.ownerAmount, 0) +
            COALESCE(r.staffAmount, 0) +
            COALESCE(r.emergencyAmount, 0) +
            COALESCE(r.othersAmount, 0) +
            COALESCE(r.rentAmount, 0) +
            COALESCE(r.loanAmount, 0) +
            COALESCE(r.waterAmount, 0) +
            COALESCE(r.lukuAmount, 0) +
            COALESCE(r.stockPurchaseAmount, 0) +
            COALESCE(r.maintenanceAmount, 0)
        ) as totalAmount

    FROM SaloonReports r

    LEFT JOIN r.saloonServiceEntity ssv

    WHERE r.branchUid = :branchUID
      AND r.isActive = true
      AND r.createdAt >= :startDate
      AND r.createdAt < :endDate

    GROUP BY
        ssv.uid,
        ssv.serviceName,
        ssv.serviceCode

    ORDER BY
        ssv.serviceName ASC
    """)
    List<SaloonServiceRevenueProjection> findCurrentSaloonRevenueByService(
            String branchUID,
            LocalDate startDate,
            LocalDate endDate
    );


    @Query("""
    SELECT
        ssv.uid as serviceUID,
        ssv.serviceName as serviceName,
        ssv.serviceCode as serviceCode,

        SUM(COALESCE(r.traAmount, 0)) as traAmount,
        SUM(COALESCE(r.ownerAmount, 0)) as ownerAmount,
        SUM(COALESCE(r.staffAmount, 0)) as staffAmount,
        SUM(COALESCE(r.emergencyAmount, 0)) as emergencyAmount,
        SUM(COALESCE(r.othersAmount, 0)) as othersAmount,
        SUM(COALESCE(r.maintenanceAmount, 0)) as maintenanceAmount,
        SUM(COALESCE(r.loanAmount, 0)) as loanAmount,
        SUM(COALESCE(r.rentAmount, 0)) as rentAmount,
        SUM(COALESCE(r.waterAmount, 0)) as waterAmount,
        SUM(COALESCE(r.lukuAmount, 0)) as lukuAmount,
        SUM(COALESCE(r.stockPurchaseAmount, 0)) as stockPurchaseAmount,
        SUM(
            COALESCE(r.traAmount, 0) +
            COALESCE(r.ownerAmount, 0) +
            COALESCE(r.staffAmount, 0) +
            COALESCE(r.emergencyAmount, 0) +
            COALESCE(r.othersAmount, 0) +
            COALESCE(r.rentAmount, 0) +
            COALESCE(r.loanAmount, 0) +
            COALESCE(r.waterAmount, 0) +
            COALESCE(r.lukuAmount, 0) +
            COALESCE(r.stockPurchaseAmount, 0) +
            COALESCE(r.maintenanceAmount, 0)
        ) as totalAmount
    FROM SaloonReports r
    LEFT JOIN r.saloonServiceEntity ssv
    WHERE r.branchUid = :branchUID
      AND r.isActive = true
      AND r.createdAt >= :startDate
      AND r.createdAt < :endDate
      AND ssv.uid IN :serviceEntityUID
    GROUP BY
        ssv.uid,
        ssv.serviceName,
        ssv.serviceCode
    ORDER BY
        ssv.serviceName ASC
""")
    Page<SaloonServiceRevenueProjection> findSaloonRevenueByStore(
            Pageable pageable,
            @Param("serviceEntityUID") List<String> serviceEntityUID,
            @Param("branchUID") String branchUID,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // A service's price is split eleven ways across these columns, so the
    // branch's revenue is their sum - the same total the revenue report shows.
    // The dashboard's three report figures in one pass over two days of rows:
    // [today's revenue, yesterday's revenue, services sold today].
    @Query("""
            SELECT
              COALESCE(SUM(CASE WHEN r.createdAt = :today THEN
                   COALESCE(r.traAmount,0) + COALESCE(r.ownerAmount,0) + COALESCE(r.staffAmount,0)
                 + COALESCE(r.emergencyAmount,0) + COALESCE(r.othersAmount,0) + COALESCE(r.loanAmount,0)
                 + COALESCE(r.rentAmount,0) + COALESCE(r.waterAmount,0) + COALESCE(r.lukuAmount,0)
                 + COALESCE(r.stockPurchaseAmount,0) + COALESCE(r.maintenanceAmount,0) ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN r.createdAt = :yesterday THEN
                   COALESCE(r.traAmount,0) + COALESCE(r.ownerAmount,0) + COALESCE(r.staffAmount,0)
                 + COALESCE(r.emergencyAmount,0) + COALESCE(r.othersAmount,0) + COALESCE(r.loanAmount,0)
                 + COALESCE(r.rentAmount,0) + COALESCE(r.waterAmount,0) + COALESCE(r.lukuAmount,0)
                 + COALESCE(r.stockPurchaseAmount,0) + COALESCE(r.maintenanceAmount,0) ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN r.createdAt = :today THEN 1 ELSE 0 END), 0)
            FROM SaloonReports r
            WHERE r.branchUid = :branchUID AND r.createdAt >= :yesterday AND r.createdAt <= :today
            """)
    List<Object[]> dashboardFigures(@Param("branchUID") String branchUID,
                                    @Param("today") LocalDate today,
                                    @Param("yesterday") LocalDate yesterday);

    // One row per day for the home page's trend line. Days with no sales are
    // simply absent - the caller fills those in as zero so the line stays
    // continuous instead of skipping closed days.
    @Query("""
            SELECT r.createdAt AS date, COALESCE(SUM(COALESCE(r.traAmount,0) + COALESCE(r.ownerAmount,0) + COALESCE(r.staffAmount,0)
                 + COALESCE(r.emergencyAmount,0) + COALESCE(r.othersAmount,0) + COALESCE(r.loanAmount,0)
                 + COALESCE(r.rentAmount,0) + COALESCE(r.waterAmount,0) + COALESCE(r.lukuAmount,0)
                 + COALESCE(r.stockPurchaseAmount,0) + COALESCE(r.maintenanceAmount,0)), 0) AS amount
            FROM SaloonReports r
            WHERE r.branchUid = :branchUID AND r.createdAt >= :startDate
            GROUP BY r.createdAt
            ORDER BY r.createdAt
            """)
    List<RevenueTrendProjection> revenueTrend(@Param("branchUID") String branchUID, @Param("startDate") LocalDate startDate);

    // Who brought in what. staffAmount is the cut that belongs to the person
    // who did the service, so summing it by staff ranks them by contribution
    // rather than by how many services they happened to touch.
    @Query("""
            SELECT st.uid AS staffUid,
                   st.firstName AS firstName,
                   st.lastName AS lastName,
                   COALESCE(SUM(COALESCE(r.staffAmount, 0)), 0) AS earned,
                   COUNT(r) AS servicesDone
            FROM SaloonReports r
            LEFT JOIN r.saloonStaff st
            WHERE r.branchUid = :branchUID
              AND r.createdAt >= :startDate
              AND st.uid IS NOT NULL
            GROUP BY st.uid, st.firstName, st.lastName
            ORDER BY COALESCE(SUM(COALESCE(r.staffAmount, 0)), 0) DESC
            """)
    List<StaffEarningsProjection> staffEarningsSince(@Param("branchUID") String branchUID, @Param("startDate") LocalDate startDate);
}
