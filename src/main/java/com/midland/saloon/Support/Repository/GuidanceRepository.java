package com.midland.saloon.Support.Repository;

import com.midland.saloon.Support.Model.Guidance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuidanceRepository extends JpaRepository<Guidance, String> {

    /** What a branch sees: the published ones, in the order set for them. */
    @Query("SELECT g FROM Guidance g WHERE g.published = true ORDER BY g.position ASC, g.createdAt DESC")
    List<Guidance> findPublished();

    /** What an admin sees: everything, drafts included. */
    @Query("SELECT g FROM Guidance g ORDER BY g.position ASC, g.createdAt DESC")
    List<Guidance> findAllOrdered();
}
