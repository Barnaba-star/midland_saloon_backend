package com.midland.saloon.Payment.Projection;

/** Money in across every payment ever recorded. */
public interface PaymentTotalsProjection {
    Long getPayments();
    Long getTotalAmount();
    Long getTotalCommission();
}
