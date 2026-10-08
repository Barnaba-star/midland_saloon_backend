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

    /** The price of ONE item. */
    private Integer buyingPrice;

    /**
     * What the whole lot cost (all `quantity` items together). Give this or
     * buyingPrice - the other is worked out from the quantity.
     */
    private Integer totalPrice;

    private String openStoreUID;
}
