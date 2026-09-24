package com.midland.saloon.Payment.Dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** How one month's takings divide between the people who run the platform. */
@Getter
@Setter
@NoArgsConstructor
public class RevenueShareDTO {

    private int year;
    private int month;

    /** Everything collected in the month. */
    private long revenue;
    private long payments;

    /**
     * What the branch-registering staff earned. Taken from the commission
     * already recorded on each payment rather than recomputed - the figure
     * here and the one on the commission report must be the same number.
     */
    private int staffPercent;
    private long staffAmount;

    /** Each director's share, multiplied by however many hold the role today. */
    private int directorPercent;
    private int directorCount;
    private long directorAmount;

    private int rootPercent;
    private long rootAmount;

    /** Whatever the three shares leave behind - never below zero. */
    private long operatingAmount;
}
