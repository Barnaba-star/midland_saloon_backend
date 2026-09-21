package com.midland.saloon.Setting.Model;

import com.midland.saloon.Utils.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Column(
            name = "branch_name",
            nullable = false,
            length = 150
    )
    private String branchName;

    @Column(
            name = "branch_code",
            nullable = false,
            length = 50
    )
    private String branchCode;


    @Column(
            name = "branch_category",
            length = 100
    )
    private String branchCategory;

    @Column(name = "region")
    private String region;

    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "status")
    private String status;

    @Column(
            name = "description",
            length = 500
    )
    private String description;

    @Column(name = "open_subscription")
    private LocalDate openSubscription;

    @Column(name = "close_subscription")
    private LocalDate closeSubscription;

    @Column(name = "subscription_amount")
    private Integer subscriptionAmount;

    @Column(name = "subscription_days")
    private Integer subscriptionDays;

    @Column(name = "subscription_status")
    private String subscriptionStatus;

    @Column(name = "subscription_phone_number")
    private String subscriptionPhoneNumber;
}