package com.midland.saloon.Saloon.Model;

import com.midland.saloon.Utils.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Table(name = "saloon_reports", indexes = {
        @Index(
                name = "idx_saloon_reports_branch",
                columnList = "branch_uid"
        ),
        @Index(
                name = "idx_saloon_reports_active",
                columnList = "is_active"
        ),
        // Every revenue report, the dashboard and the trend line filter on these two.
        @Index(name = "idx_saloon_reports_branch_created", columnList = "branch_uid, created_at"),
        @Index(name = "idx_saloon_reports_sales", columnList = "saloon_sales"),
        @Index(name = "idx_saloon_reports_staff", columnList = "saloon_staff_uid")
})
public class SaloonReports extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_staff_uid")
    private SaloonStaff saloonStaff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_service_uid")
    private SaloonServiceEntity saloonServiceEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_sales")
    private SaloonSales saloonSales;

    @Column(name = "tra_amount")
    private Integer traAmount;

    @Column(name = "emergency_amount")
    private Integer emergencyAmount;

    @Column(name = "other_amount")
    private Integer othersAmount;

    @Column(name = "owner_amount")
    private Integer ownerAmount;

    @Column(name = "staff_amount")
    private Integer staffAmount;

    @Column(name = "maintenance_amount")
    private Integer maintenanceAmount;

    @Column(name = "luku_percent")
    private Integer lukuAmount;

    @Column(name = "water_percent")
    private Integer waterAmount;

    @Column(name = "rent_percent")
    private Integer rentAmount;

    @Column(name = "loan_percent")
    private Integer loanAmount;

    @Column(name = "stock_purchase_percent")
    private Integer stockPurchaseAmount;
}
