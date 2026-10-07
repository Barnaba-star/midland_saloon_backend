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
@Table(name = "service_store_report", indexes = {
        @Index(
                name = "idx_service_store_report_branch",
                columnList = "branch_uid"
        ),
        @Index(
                name = "idx_service_store_report_active",
                columnList = "is_active"
        ),
        @Index(name = "idx_service_store_report_store_open", columnList = "store_open_uid"),
        @Index(name = "idx_service_store_report_report", columnList = "saloon_report_uid")
})
public class ServiceAndStoreReport extends TenantEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saloon_report_uid")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SaloonReports saloonReports;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_open_uid")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private StoreOpen storeOpen;

    @Column(name = "shared_amount")
    private Integer sharedAmount;
}
