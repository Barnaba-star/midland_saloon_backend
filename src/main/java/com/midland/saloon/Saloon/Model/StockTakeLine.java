package com.midland.saloon.Saloon.Model;

import com.midland.saloon.Utils.TenantEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/** One store item of a stock take, with its name as it was that day. Saloon items have no packs: unitsPerPack is 1. */
@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_take_lines", indexes = {
        @Index(name = "idx_stock_take_line_parent", columnList = "stock_take_uid"),
        @Index(name = "idx_stock_take_line_store", columnList = "store_uid")
})
public class StockTakeLine extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_take_uid", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private StockTake stockTake;

    /** The store item counted (Store.uid). */
    @Column(name = "store_uid", nullable = false)
    private String storeUid;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_code")
    private String serviceCode;

    @Column(name = "unit")
    private String unit;

    @Column(name = "pack_unit")
    private String packUnit;

    @Column(name = "units_per_pack")
    private Integer unitsPerPack;

    /** What the system held: the item's unopened units (Store.notUsedQuantity). */
    @Column(name = "system_units")
    private Integer systemUnits;

    @Column(name = "counted_units")
    private Integer countedUnits;

    /** Counted minus system: below zero is missing. */
    @Column(name = "difference_units")
    private Integer differenceUnits;

    /** The difference at buying price. */
    @Column(name = "difference_value")
    private Long differenceValue;
}
