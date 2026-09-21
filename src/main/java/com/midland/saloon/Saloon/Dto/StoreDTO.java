package com.midland.saloon.Saloon.Dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreDTO {
    private String uid;
    private String nameOfStore;

    private String codeOfStore;

    private Integer quantity;

    private String saloonServiceEntityUID;

    private String description;

    private Integer buyingPrice;

    private String openStoreUID;
}
