package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.SaloonSales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Read-only queries behind the profit, best-seller, peak-hour and pots-ledger
 * reports. A saloon sale line is one service done, with no price or cost of
 * its own: revenue is the service's price, and the cost of a service is not kept.
 */
public interface InsightRepository extends JpaRepository<SaloonSales, String> {

    /** Per service over [from, to): uid, name, code, services done, revenue at the service's price. */
    @Query("SELECT s.uid, s.serviceName, s.serviceCode, COUNT(b), SUM(COALESCE(s.price, 0)) " +
           "FROM SaloonSales b JOIN b.saloonServiceEntity s " +
           "WHERE b.branchUid = :branchUID AND b.isActive = true AND b.createdAt >= :from AND b.createdAt < :to " +
           "GROUP BY s.uid, s.serviceName, s.serviceCode")
    List<Object[]> salesByService(@Param("branchUID") String branchUID, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Store items still unopened that were not opened at all in [from, to):
     * uid, name, code, unopened units, buying price per unit, last opened date.
     */
    @Query("SELECT st.uid, st.nameOfStore, st.codeOfStore, st.notUsedQuantity, st.buyingPrice, " +
           "(SELECT MAX(o2.createdAt) FROM StoreOpen o2 WHERE o2.store = st AND o2.isActive = true) " +
           "FROM Store st WHERE st.branchUid = :branchUID AND st.isActive = true AND COALESCE(st.notUsedQuantity, 0) > 0 " +
           "AND NOT EXISTS (SELECT 1 FROM StoreOpen o WHERE o.store = st AND o.isActive = true " +
           "AND o.createdAt >= :from AND o.createdAt < :to) ORDER BY st.nameOfStore")
    List<Object[]> idleStoreItems(@Param("branchUID") String branchUID, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Sales by ISO weekday (1 Monday .. 7 Sunday) and hour: when the line was put on
     * the bill, or for lines from before that was kept, when the bill was paid.
     */
    @Query(value = "SELECT CAST(EXTRACT(ISODOW FROM t) AS int) AS dow, CAST(EXTRACT(HOUR FROM t) AS int) AS hr, " +
                   "SUM(COALESCE(price, 0)) AS amount, COUNT(*) AS units FROM (" +
                   "  SELECT COALESCE(b.sold_at, so.paid_at) AS t, sv.price FROM saloon_sales b " +
                   "  LEFT JOIN sales_opened so ON so.uid = b.sales_opened " +
                   "  LEFT JOIN saloon_services sv ON sv.uid = b.saloon_service_uid " +
                   "  WHERE b.branch_uid = :branchUID AND b.is_active = true AND b.created_at >= :from AND b.created_at < :to" +
                   ") x WHERE t IS NOT NULL GROUP BY 1, 2", nativeQuery = true)
    List<Object[]> salesByHour(@Param("branchUID") String branchUID, @Param("from") LocalDate from, @Param("to") LocalDate to);

    /** Every pot of the branch since it began: name, collected, spent. */
    @Query("SELECT i.name, SUM(COALESCE(i.income, 0)), SUM(COALESCE(i.expenses, 0)) FROM IncomeExpenses i " +
           "WHERE i.branchUid = :branchUID AND i.isActive = true GROUP BY i.name")
    List<Object[]> potsAllTime(@Param("branchUID") String branchUID);

    /** The same for the weeks starting in [from, to). */
    @Query("SELECT i.name, SUM(COALESCE(i.income, 0)), SUM(COALESCE(i.expenses, 0)) FROM IncomeExpenses i " +
           "WHERE i.branchUid = :branchUID AND i.isActive = true AND i.weekStartDate >= :from AND i.weekStartDate < :to GROUP BY i.name")
    List<Object[]> potsInWeeks(@Param("branchUID") String branchUID, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
