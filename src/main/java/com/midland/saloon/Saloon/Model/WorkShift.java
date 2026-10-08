package com.midland.saloon.Saloon.Model;

import com.midland.saloon.Utils.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * A cashier's shift (zamu): opened before the first sale, closed when they
 * stop, then handed over - the cash-up of its money and a count of the store.
 * Bills are opened, added to and paid only while the login's shift is OPEN,
 * and a new one can't be opened until the last is cashed up - so every
 * payment falls in exactly one cash-up, and any money short or store item
 * missing stays on the shift of whoever opened it.
 */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "work_shifts", indexes = {
        @Index(name = "idx_work_shift_branch_cashier", columnList = "branch_uid, cashier_email, status"),
        @Index(name = "idx_work_shift_branch_opened", columnList = "branch_uid, opened_at")
})
public class WorkShift extends TenantEntity {

    public static final String OPEN = "OPEN";
    /** Closed and waiting for its cash-up. */
    public static final String CLOSED = "CLOSED";
    public static final String CASHED_UP = "CASHED_UP";

    /** The login selling in it - SalesOpened.paidBy. */
    @Column(name = "cashier_email", nullable = false)
    private String cashierEmail;

    @Column(name = "cashier_name")
    private String cashierName;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "status", length = 12, nullable = false)
    private String status = OPEN;

    @Column(name = "cash_up_uid")
    private String cashUpUid;
}
