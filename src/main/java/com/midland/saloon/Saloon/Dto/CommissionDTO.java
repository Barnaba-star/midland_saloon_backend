package com.midland.saloon.Saloon.Dto;

import com.midland.saloon.Saloon.Model.SaloonServiceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CommissionDTO {
    private String uid;
    private String saloonServiceUID;


    private Integer staffPercent;


    private Integer ownerPercent;

    private Integer  traPercent;


    private Integer emergencyPercent;


    private Integer totalPercent;


    private Integer maintenancePercent;

    private Integer otherPercent;

    private Integer lukuPercent;

    private Integer waterPercent;

    private Integer rentPercent;

    private Integer loanPercent;

    private Integer stockPurchasePercent;
}
