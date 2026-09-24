package com.midland.saloon.Payment.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One person on the payroll, and what the bank should send them. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PayrollLineDTO {

    private String uid;

    /** ROOT, DIRECTOR or STAFF - how they came to be owed this. */
    private String role;

    private String name;

    private String phone;

    /**
     * Where the money goes. Not yet collected anywhere, so null for now -
     * the column is here so the sheet does not change shape the day it is.
     */
    private String accountNumber;

    /** What the month earned them. */
    private long earned;

    /** What has already been recorded as settled. */
    private long paid;

    /** What the bank is being asked to send: earned less already paid. */
    private long toPay;
}
