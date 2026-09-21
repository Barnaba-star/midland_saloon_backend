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
        )
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
}
