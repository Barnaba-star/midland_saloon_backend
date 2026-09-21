package com.midland.saloon.Saloon.Projection;

import java.math.BigDecimal;

public interface SaloonServiceRevenueProjection {

    String getServiceUID();

    String getServiceName();

    String getServiceCode();

    String getUsageType();

    BigDecimal getTraAmount();

    BigDecimal getOwnerAmount();

    BigDecimal getStaffAmount();

    BigDecimal getEmergencyAmount();

    BigDecimal getOthersAmount();

    BigDecimal getMaintenanceAmount();
    BigDecimal getLoanAmount();
    BigDecimal getRentAmount();
    BigDecimal getWaterAmount();
    BigDecimal getLukuAmount();
    BigDecimal getStockPurchaseAmount();
}
