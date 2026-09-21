package com.midland.saloon.Saloon.Dto;

import com.midland.saloon.Saloon.Model.SaloonServiceEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaloonSalesDTO {
    private String uid;
    private String saloonStaffUID;
    private List<String> saloonServiceUID;
    private String paymentMethod;
    private String salesOpenedUID;
}
