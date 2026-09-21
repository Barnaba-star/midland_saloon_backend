package com.midland.saloon.Saloon.Model;

import com.midland.saloon.Utils.TenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
@Table(name = "saloon_services", indexes = {
        @Index(
                name = "idx_saloon_services_branch",
                columnList = "branch_uid"
        ),
        @Index(
                name = "idx_saloon_services_active",
                columnList = "is_active"
        )
})
public class SaloonServiceEntity extends TenantEntity {
    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_code")
    private String serviceCode;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Integer price;

    @Column(name = "duration")
    private Integer duration;


    @Column(name = "status")
    private String status;

    @Column(name = "usage_type")
    private String usageType;

}
