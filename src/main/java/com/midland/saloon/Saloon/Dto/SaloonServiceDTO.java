package com.midland.saloon.Saloon.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaloonServiceDTO {
    private String uid;
    @NotBlank(message = "Service name is required")
    private String serviceName;


    @NotBlank(message = "Service code is required")
    private String serviceCode;


    private String description;


    @NotNull(message = "Price is required")
    private Integer price;


    @NotNull(message = "Duration is required")
    private Integer duration;


    private String status;

    private String usageType;

}
