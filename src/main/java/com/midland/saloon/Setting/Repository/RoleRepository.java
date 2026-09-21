package com.midland.saloon.Setting.Repository;

import com.midland.saloon.Setting.Model.Role;
import com.midland.saloon.Setting.Projection.BranchProjection;
import com.midland.saloon.Uaa.Model.Permission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    @Query("SELECT r FROM Role r WHERE r.name = :roleName")
    Role findFirstByName(@Param("roleName") String roleName);

    Role findByCode(String code);
    @Query("""
    SELECT r
    FROM Role r
    WHERE r.isActive = true
    ORDER BY r.createdAt DESC
""")
    Page<Role> findRolePage(Pageable pageable);

    @Query(
            value = """
        SELECT p.*
        FROM roles r
        LEFT JOIN role_permission rp
            ON r.uid = rp.role_uid
        LEFT JOIN permissions p
            ON rp.permission_uid = p.uid
        WHERE r.uid = :roleUID
        """,
            nativeQuery = true
    )
    List<Permission> findPermissionByRole(@Param("roleUID") String roleUID);



    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.permission  WHERE r.uid=:roleUID")
    List<Role> findRoleAndPermission(String roleUID);

    @Query("""
    SELECT
        r.uid AS uid,
        r.name AS name,
        r.code AS code,
        r.category AS category
    FROM Role r
    """)
    List<BranchProjection> findRoleByBranch();




}
