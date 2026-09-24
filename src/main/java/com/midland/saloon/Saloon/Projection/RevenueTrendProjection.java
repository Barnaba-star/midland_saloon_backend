package com.midland.saloon.Saloon.Projection;

import java.time.LocalDate;

/** One point on the branch's revenue line: a day, and what it took. */
public interface RevenueTrendProjection {
    LocalDate getDate();
    Long getAmount();
}
