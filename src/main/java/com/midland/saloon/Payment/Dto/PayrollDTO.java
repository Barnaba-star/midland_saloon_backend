package com.midland.saloon.Payment.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/** A month's payment schedule, in the shape it is taken to the bank. */
@Getter
@Setter
@NoArgsConstructor
public class PayrollDTO {

    private int year;
    private int month;

    /** Stamped on the printed sheet, so two copies can be told apart. */
    private LocalDateTime generatedAt;

    /** The month's subscription income the shares were worked out from. */
    private long revenue;

    private long totalToPay;

    private int recipients;

    /**
     * How many of those have no phone number on file. A bank needs one, so
     * this is worth saying out loud before the sheet is printed rather than
     * discovered at the counter.
     */
    private int missingPhone;

    private List<PayrollLineDTO> lines;
}
