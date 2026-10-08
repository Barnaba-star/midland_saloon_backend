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
import java.util.Optional;

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
      AND (LOWER(s.nameOfStore) LIKE CONCAT('%', :q, '%')
           OR LOWER(s.codeOfStore) LIKE CONCAT('%', :q, '%')
           OR LOWER(COALESCE(ser.serviceName, '')) LIKE CONCAT('%', :q, '%'))
    ORDER BY s.nameOfStore
""")
    Page<SaloonProjection> findStorePage(Pageable pageable, @Param("branchUID") String branchUID, @Param("q") String q);

    /** [store items opened for use, store items] - the dashboard's store figures in one select. */
    @Query("""
            SELECT (SELECT COUNT(o) FROM StoreOpen o WHERE o.branchUid = :branchUID AND o.status = 'OPEN'), COUNT(s)
            FROM Store s WHERE s.branchUid = :branchUID
            """)
    List<Object[]> storeCounts(@Param("branchUID") String branchUID);

    /** Every active store item of the branch - what a stock take goes through. */
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Store s WHERE s.branchUid = :branchUID AND s.isActive = true ORDER BY s.nameOfStore")
    java.util.List<Store> findCountable(@org.springframework.data.repository.query.Param("branchUID") String branchUID);

    /** One store item, locked for the rest of the transaction. */
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT s FROM Store s WHERE s.uid = :uid AND s.branchUid = :branchUID")
    java.util.Optional<Store> findForUpdate(@org.springframework.data.repository.query.Param("uid") String uid,
                                            @org.springframework.data.repository.query.Param("branchUID") String branchUID);

    /** One store item with its service - what the JSON of a store item carries - in one select. */
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.saloonServiceEntity WHERE s.uid = :uid")
    Optional<Store> findWithService(@Param("uid") String uid);
}
