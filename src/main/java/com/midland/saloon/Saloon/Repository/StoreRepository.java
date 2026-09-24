package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.Store;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository  extends JpaRepository<Store, String> {
    @Query("""
    SELECT
        s.uid AS uid,
        s.nameOfStore AS nameOfStore,
        s.codeOfStore AS codeOfStore,
        s.description AS description,
        s.quantity AS quantity,
        s.createdAt AS openedDate,
     
        s.status AS status,
        ser.serviceName AS serviceName
    FROM Store s
    LEFT JOIN s.saloonServiceEntity ser
    WHERE s.branchUid = :branchUID
    
""")
    List<SaloonProjection> findStoreList(String branchUID);

    @Query("""
    SELECT
        s.uid AS uid,
        s.nameOfStore AS nameOfStore,
        s.codeOfStore AS codeOfStore,
        s.description AS description,
        s.quantity AS quantity,
        s.createdAt AS openedDate,
     
        s.status AS status,
        s.buyingPrice AS buyingPrice,
        s.totalQuantityPrice AS totalQuantityPrice,
        s.usedQuantity AS usedQuantity,
        s.notUsedQuantity AS notUsedQuantity,
        ser.serviceName AS serviceName
    FROM Store s
    LEFT JOIN s.saloonServiceEntity ser
    WHERE s.branchUid = :branchUID
   
""")
    Page<SaloonProjection> findStorePage(Pageable pageable, String branchUID);

    @Query("SELECT COUNT(s) FROM Store s WHERE s.branchUid = :branchUID")
    long countStores(@Param("branchUID") String branchUID);
}
