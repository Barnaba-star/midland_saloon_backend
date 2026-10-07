package com.midland.saloon.Saloon.Model;

import com.midland.saloon.Utils.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "saloon_sales", indexes = {
        @Index(
                name = "idx_saloon_sales_branch",
                columnList = "branch_uid"
        ),
        @Index(
                name = "idx_saloon_sales_active",
                columnList = "is_active"
        ),
        // Today's lines and the insight reports by period.
        @Index(name = "idx_saloon_sales_branch_created", columnList = "branch_uid, created_at"),
        // The lines of one bill.
        @Index(name = "idx_saloon_sales_bill", columnList = "sales_opened")
})
public class SaloonSales extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_staff_uid")
    private SaloonStaff saloonStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_service_uid")
    private SaloonServiceEntity saloonServiceEntity;

    @Column(name = "payment_method")
    private String paymentMethod;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_opened")
    private SalesOpened salesOpened;





    @Column(name = "status")
    private String status = "ACTIVE";

    /** When the line was put on the bill, to the second - what the peak-hours report reads. */
    @Column(name = "sold_at")
    private java.time.LocalDateTime soldAt;
}
