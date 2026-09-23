package com.midland.saloon.Payment.Projection;

import java.time.LocalDate;

public interface PayoutTotalProjection {
    String getStaffUid();
    Long getAmountPaid();
    LocalDate getLastPaidAt();
}
