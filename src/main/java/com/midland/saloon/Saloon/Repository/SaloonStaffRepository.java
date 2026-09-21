package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.SaloonStaff;
import com.midland.saloon.Saloon.Projection.SaloonProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaloonStaffRepository extends JpaRepository<SaloonStaff, String> {
    @Query("SELECT s FROM SaloonStaff s WHERE s.uid=:uid AND s.branchUid=:branchUID AND s.isActive=true")
    Optional<SaloonStaff> findSaloonStaffByUID(String uid, String branchUID);

    @Query("""
            SELECT s.uid as uid, s.firstName as firstName, s.middleName as middleName, s.lastName as lastName, s.dateOfBirth as dateOfBirth, s.phoneNumber as phoneNumber,
            s.description as description, s.gender as gender, s.saloonCategory as saloonCategory, s.isActive as active FROM SaloonStaff s  WHERE s.branchUid=:branchUID AND s.isActive=true
            """)
    List<SaloonProjection> findSaloonStaffList(String branchUID);


    @Query("""
             SELECT s.uid as uid, s.firstName as firstName, s.middleName as middleName, s.lastName as lastName, s.dateOfBirth as dateOfBirth, s.phoneNumber as phoneNumber,
             s.description as description, s.gender as gender, s.saloonCategory as saloonCategory, s.isActive as active FROM SaloonStaff s  WHERE s.branchUid=:branchUID AND s.isActive=true
            """)
    Page<SaloonProjection> findSaloonStaffPage(Pageable pageable, String branchUID);
}
