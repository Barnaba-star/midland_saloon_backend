package com.midland.saloon.Setting.Repository;

import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Projection.BranchProjection;
import com.midland.saloon.Uaa.Model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String> {
    @Query("SELECT b.uid AS uid, b.branchCode AS branchCode, b.branchCategory AS branchCategory, b.branchName as name FROM Branch b WHERE b.isActive=true")
    List<BranchProjection> findBranchList();

    @Query("SELECT b FROM Branch b WHERE b.branchName =:rootBranch")
    Optional<Branch> findRootBranch(String rootBranch);

    Branch findByBranchCode(String branchName);

    @Query(value = """
    SELECT COALESCE(
        MAX(
            CAST(
                SUBSTRING(branch_code FROM '[0-9]+$')
                AS INTEGER
            )
        ),
        0
    )
    FROM branches
    WHERE UPPER(region) = UPPER(:region)
    """, nativeQuery = true)
    Integer findLastBranchSequenceByRegion(
            @Param("region") String region
    );



}
