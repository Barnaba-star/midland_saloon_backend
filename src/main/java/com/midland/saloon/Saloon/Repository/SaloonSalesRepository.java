package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.SaloonSales;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaloonSalesRepository extends JpaRepository<SaloonSales, String> {

    @Query("SELECT s FROM SaloonSales s WHERE s.uid=:uid AND s.branchUid=:branchUID")
    Optional<SaloonSales> findSalesByUID(String uid, String branchUID);

    /** How many live lines a bill has - an empty bill has none, whatever they cost. */
    @Query("SELECT COUNT(s) FROM SaloonSales s WHERE s.salesOpened.uid = :billUid AND s.isActive = true")
    long countLines(@org.springframework.data.repository.query.Param("billUid") String billUid);

    @Query("""
            SELECT s.uid as uid,  s.createdAt as salesTime, so.paymentMethod as paymentName,
            so.salesCode as salesCode, so.status as status,
            st.firstName as firstName, st.middleName as middleName, st.lastName as lastName,
            ss.serviceName as serviceName, ss.serviceCode as serviceCode, ss.price as price
            FROM SaloonSales s LEFT JOIN s.saloonStaff st LEFT JOIN s.saloonServiceEntity ss
            LEFT JOIN SalesOpened so WHERE s.uid=:saloonSalesUID AND s.branchUid=:branchUID AND s.isActive=true
            """)
    Optional<SaloonProjection> findSaloonSalesByUID(String saloonSalesUID, String branchUID);

    @Query("""
    SELECT
        s.uid as uid,
        s.createdAt as salesTime,
        so.paymentMethod as paymentName,
        so.salesCode as salesCode,
        so.status as status,
        st.firstName as firstName,
        st.middleName as middleName,
        st.lastName as lastName,
        ss.serviceName as serviceName,
        ss.serviceCode as serviceCode,
        ss.price as price
    FROM SaloonSales s
    JOIN s.salesOpened so
    LEFT JOIN s.saloonStaff st
    LEFT JOIN s.saloonServiceEntity ss
    WHERE so.uid = :saloonOpenUID
      AND s.branchUid = :branchUID
      AND s.isActive = true
""")
    List<SaloonProjection> findSaloonSalesList(String branchUID, String saloonOpenUID);

    @Query("""
    SELECT
        s.uid as uid,
        s.createdAt as salesTime,
        so.paymentMethod as paymentName,
        so.salesCode as salesCode,
        so.status as status,
        st.firstName as firstName,
        st.middleName as middleName,
        st.lastName as lastName,
        ss.serviceName as serviceName,
        ss.serviceCode as serviceCode,
        ss.price as price
    FROM SaloonSales s
    JOIN s.salesOpened so
    LEFT JOIN s.saloonStaff st
    LEFT JOIN s.saloonServiceEntity ss
    WHERE s.branchUid = :branchUID
      AND s.isActive = true
      AND s.status = 'ACTIVE'
      AND s.createdAt = :localDate
""")
    List<SaloonProjection> findSaloonSalesListActiveTrue(String branchUID, LocalDate localDate);

    @Query("""
           SELECT s.uid as uid,  s.createdAt as salesTime, so.paymentMethod as paymentName,
            so.salesCode as salesCode, so.status as status,
            st.firstName as firstName, st.middleName as middleName, st.lastName as lastName,
            ss.serviceName as serviceName, ss.serviceCode as serviceCode, ss.price as price
            FROM SaloonSales s LEFT JOIN s.saloonStaff st LEFT JOIN s.saloonServiceEntity ss
            LEFT JOIN SalesOpened so WHERE  s.branchUid=:branchUID AND s.isActive=true
           """)
    Page<SaloonProjection> findSaloonSalesPage(Pageable pageable, String branchUID);
}
