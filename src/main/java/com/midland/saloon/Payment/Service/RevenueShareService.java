package com.midland.saloon.Payment.Service;

import com.midland.saloon.Payment.Dto.ShareRecipientDTO;
import com.midland.saloon.Payment.Dto.StaffCommissionDTO;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Utils.Responses.ResponseList;
import java.util.ArrayList;
import java.util.List;
import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Payment.Model.CommissionPayout;
import com.midland.saloon.Payment.Repository.CommissionPayoutRepository;
import org.springframework.transaction.annotation.Transactional;
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
    private static final String ROOT_ROLE_CODE = "ROOT";
    private static final String STAFF_ROLE_CODE = "STAFF";

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PlatformSettingService platformSettingService;
    private final UserRepository userRepository;
    private final CommissionService commissionService;
    private final CommissionPayoutRepository commissionPayoutRepository;

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

    private static YearMonth periodOf(Integer year, Integer month) {
        return (year == null || month == null) ? YearMonth.now() : YearMonth.of(year, month);
    }

    private static long percentOf(long amount, long percent) {
        return Math.round(amount * percent / 100.0);
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * Who is owed the share a row on the summary stands for.
     *
     * Staff come from the commission report rather than a fresh calculation:
     * that report is what they are actually paid from, and two ways of
     * working out the same figure would eventually disagree in front of
     * someone expecting money.
     */
    public ResponseList<ShareRecipientDTO> findShareRecipients(String role, Integer year, Integer month) {
        String wanted = role == null ? "" : role.trim().toUpperCase();

        if (STAFF_ROLE_CODE.equals(wanted)) {
            List<ShareRecipientDTO> staff = new ArrayList<>();
            for (StaffCommissionDTO row : commissionService.staffCommissionReport(year, month).getData()) {
                staff.add(new ShareRecipientDTO(
                        row.getStaffUid(),
                        row.getStaffName(),
                        row.getUsername(),
                        row.getCommissionDue(),
                        row.getCommissionPaid(),
                        row.getOutstanding()
                ));
            }
            return new ResponseList<>(staff);
        }

        RevenueShareDTO share = findRevenueShare(year, month).getData();

        if (DIRECTOR_ROLE_CODE.equals(wanted)) {
            // Each director is due the same percentage, so the pool divides
            // evenly by definition rather than by choice.
            long each = share.getDirectorCount() == 0
                    ? 0
                    : share.getDirectorAmount() / share.getDirectorCount();
            return new ResponseList<>(toRecipients(DIRECTOR_ROLE_CODE, each, periodOf(year, month)));
        }

        if (ROOT_ROLE_CODE.equals(wanted)) {
            List<User> roots = userRepository.findAllByRoleCode(ROOT_ROLE_CODE);
            // ROOT's share is one share for the role, not one each - split it
            // rather than paying it out once per holder.
            long each = roots.isEmpty() ? 0 : share.getRootAmount() / roots.size();
            return new ResponseList<>(toRecipients(ROOT_ROLE_CODE, each, periodOf(year, month)));
        }

        return new ResponseList<>("Unknown share");
    }

    private List<ShareRecipientDTO> toRecipients(String roleCode, long each, YearMonth period) {
        List<ShareRecipientDTO> recipients = new ArrayList<>();
        for (User user : userRepository.findAllByRoleCode(roleCode)) {
            long paid = commissionPayoutRepository.paidForShare(
                    user.getUid(), roleCode, period.getYear(), period.getMonthValue());
            recipients.add(new ShareRecipientDTO(
                    user.getUid(),
                    displayName(user),
                    user.getUsername(),
                    each,
                    paid,
                    // Clamped: a share lowered after someone was paid should
                    // read as nothing owed, not as money to claw back.
                    Math.max(0, each - paid)
            ));
        }
        return recipients;
    }

    private static String displayName(User user) {
        String name = String.format("%s %s",
                user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()).trim();
        return name.isBlank() ? user.getUsername() : name;
    }

    /**
     * Records a payout against one person's share.
     *
     * Staff are handed straight to the commission service rather than paid a
     * second way from here - one route to staff money, not two that could
     * disagree about what is still owed.
     */
    @Transactional
    public Response<String> payShare(String role, String uid, Integer year, Integer month, String note) {
        String wanted = role == null ? "" : role.trim().toUpperCase();
        if (uid == null || uid.isBlank()) {
            return new Response<>("Provide the person to pay");
        }

        if (STAFF_ROLE_CODE.equals(wanted)) {
            Response<CommissionPayout> paid = commissionService.payStaffCommission(uid, year, month, note);
            return paid.getData() != null
                    ? new Response<>("Payout recorded")
                    : new Response<>(paid.getMessage());
        }

        if (!DIRECTOR_ROLE_CODE.equals(wanted) && !ROOT_ROLE_CODE.equals(wanted)) {
            return new Response<>("Unknown share");
        }

        // Directors and ROOT are paid from the platform's own income, so only
        // ROOT records these - a director settling their own share would be
        // signing their own cheque.
        User payer = LoggerUser.getUser();
        boolean isRoot = Boolean.TRUE.equals(payer.getIsRoot())
                || (payer.getRoles() != null && payer.getRoles().stream()
                        .anyMatch(r -> ROOT_ROLE_CODE.equals(r.getCode())));
        if (!isRoot) {
            return new Response<>("Only ROOT can record this payout");
        }

        YearMonth period = (year == null || month == null) ? YearMonth.now() : YearMonth.of(year, month);

        ShareRecipientDTO recipient = findShareRecipients(wanted, period.getYear(), period.getMonthValue())
                .getData().stream()
                .filter(r -> uid.equals(r.getUid()))
                .findFirst()
                .orElse(null);

        if (recipient == null) {
            return new Response<>("This person does not hold that role");
        }
        if (recipient.getOutstanding() <= 0) {
            return new Response<>(recipient.getAmount() <= 0
                    ? "Nothing was earned for this share in this period"
                    : "This share has already been paid");
        }

        CommissionPayout payout = new CommissionPayout();
        payout.setRoleCode(wanted);
        payout.setStaffUid(uid);
        payout.setStaffName(recipient.getName());
        payout.setPeriodYear(period.getYear());
        payout.setPeriodMonth(period.getMonthValue());
        payout.setAmount((int) recipient.getOutstanding());
        payout.setPaidAt(LocalDate.now());
        payout.setNote(note);
        payout.setPaidByUid(payer.getUid());
        payout.setPaidByName(displayName(payer));

        commissionPayoutRepository.save(payout);
        return new Response<>("Payout recorded");
    }

}
