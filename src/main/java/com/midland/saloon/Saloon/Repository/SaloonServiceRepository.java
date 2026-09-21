package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.SaloonServiceEntity;
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
public interface SaloonServiceRepository extends JpaRepository<SaloonServiceEntity, String> {
    @Query("SELECT s FROM SaloonServiceEntity s WHERE s.uid=:saloonServiceUID AND s.branchUid=:branchUID")
    Optional<SaloonServiceEntity> findSaloonServiceByUID(@Param("saloonServiceUID") String saloonServiceUID, String branchUID);

    @Query("""
           SELECT s.uid AS uid, s.serviceName AS serviceName,
              s.serviceCode AS serviceCode,
              s.description AS description,
              s.price AS price,
              s.duration AS duration,
              s.status AS status,
              s.usageType AS usageType
           FROM SaloonServiceEntity s
           WHERE s.branchUid = :branchUID
           """)
    List<SaloonProjection> findAllSaloonServiceList(
            @Param("branchUID") String branchUID
    );

    @Query("""
           SELECT s.uid AS uid, s.serviceName AS serviceName,
              s.serviceCode AS serviceCode,
              s.description AS description,
              s.price AS price,
              s.duration AS duration,
              s.status AS status,
              s.usageType AS usageType
           FROM SaloonServiceEntity s
           WHERE s.branchUid = :branchUID
           """)
    Page<SaloonProjection> findSaloonServicePage(
            Pageable pageable, @Param("branchUID") String branchUID
    );
}
