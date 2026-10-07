package com.midland.saloon.Support.Repository;

import com.midland.saloon.Support.Model.BranchMessageReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchMessageReplyRepository extends JpaRepository<BranchMessageReply, String> {

    @Query("SELECT r FROM BranchMessageReply r WHERE r.message.uid = :messageUid ORDER BY r.sentAt ASC")
    List<BranchMessageReply> findForMessage(@Param("messageUid") String messageUid);

    /** Reply counts for a page of threads in one select: message uid, count. Threads with none are absent. */
    @Query("SELECT r.message.uid, COUNT(r) FROM BranchMessageReply r WHERE r.message.uid IN :messageUids GROUP BY r.message.uid")
    List<Object[]> countForMessages(@Param("messageUids") java.util.Collection<String> messageUids);
}
