package com.midland.saloon.Payment.Service;

import com.midland.saloon.Payment.Dto.RevenueShareDTO;
import com.midland.saloon.Payment.Projection.PaymentTotalsProjection;
import com.midland.saloon.Payment.Repository.SubscriptionPaymentRepository;
import com.midland.saloon.Setting.Model.PlatformSetting;
import com.midland.saloon.Setting.Service.PlatformSettingService;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Splits a month's subscription income between staff, directors, root and
 * what is left to run on.
 *
 * The director count is read rather than fixed: adding a second director
 * doubles that share on its own, with nothing to remember to change.
 */
@Service
@RequiredArgsConstructor
public class RevenueShareService {

    private static final String DIRECTOR_ROLE_CODE = "DIRECTOR";

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PlatformSettingService platformSettingService;
    private final UserRepository userRepository;

    public Response<RevenueShareDTO> findRevenueShare(Integer year, Integer month) {
        YearMonth period = (year == null || month == null) ? YearMonth.now() : YearMonth.of(year, month);
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        PaymentTotalsProjection totals = subscriptionPaymentRepository.totalsBetween(start, end);
        long revenue = totals == null || totals.getTotalAmount() == null ? 0 : totals.getTotalAmount();
        long staffAmount = totals == null || totals.getTotalCommission() == null ? 0 : totals.getTotalCommission();

        PlatformSetting setting = platformSettingService.current();
        int directorCount = userRepository.findAllByRoleCode(DIRECTOR_ROLE_CODE).size();

        RevenueShareDTO share = new RevenueShareDTO();
        share.setYear(period.getYear());
        share.setMonth(period.getMonthValue());
        share.setRevenue(revenue);
        share.setPayments(totals == null || totals.getPayments() == null ? 0 : totals.getPayments());

        share.setStaffPercent(orZero(setting.getCommissionPercent()));
        share.setStaffAmount(staffAmount);

        share.setDirectorPercent(orZero(setting.getDirectorPercent()));
        share.setDirectorCount(directorCount);
        share.setDirectorAmount(percentOf(revenue, (long) share.getDirectorPercent() * directorCount));

        share.setRootPercent(orZero(setting.getRootPercent()));
        share.setRootAmount(percentOf(revenue, share.getRootPercent()));

        // Clamped at zero: percentages that add past 100 would otherwise show
        // a negative running budget, which reads as a debt rather than as the
        // misconfiguration it is.
        long shared = share.getStaffAmount() + share.getDirectorAmount() + share.getRootAmount();
        share.setOperatingAmount(Math.max(0, revenue - shared));

        return new Response<>(share);
    }

    private static long percentOf(long amount, long percent) {
        return Math.round(amount * percent / 100.0);
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }
}
