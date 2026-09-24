package com.midland.saloon.Payment.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Payment.Dto.PayrollDTO;
import com.midland.saloon.Payment.Dto.PayrollLineDTO;
import com.midland.saloon.Payment.Dto.RevenueShareDTO;
import com.midland.saloon.Payment.Dto.ShareRecipientDTO;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The month's share-out written as a payment schedule.
 *
 * It calculates nothing of its own. RevenueShareService already works out
 * what each person is owed, and the Payments screen already shows it - two
 * ways of arriving at the same figure would eventually disagree in front of
 * a bank teller. This reads that answer and lays it out to be printed.
 *
 * What it does add is the difference between what someone earned and what
 * they are still owed. A sheet handed to a bank is an instruction to send
 * money, so it carries what is outstanding, not what was earned - otherwise
 * anyone already settled would be paid twice.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollService {

    /** Ordered the way the money splits, so the sheet reads top-down. */
    private static final List<String> ROLES = List.of("ROOT", "DIRECTOR", "STAFF");

    private final RevenueShareService revenueShareService;
    private final UserRepository userRepository;

    public Response<PayrollDTO> findPayroll(Integer year, Integer month) {
        log.info(LoggerUser.getEmail() + " is accessing the payroll");

        YearMonth period = (year == null || month == null) ? YearMonth.now() : YearMonth.of(year, month);

        List<PayrollLineDTO> lines = new ArrayList<>();

        for (String role : ROLES) {
            List<ShareRecipientDTO> recipients =
                    revenueShareService.findShareRecipients(role, period.getYear(), period.getMonthValue()).getData();
            if (recipients == null) {
                continue;
            }
            for (ShareRecipientDTO recipient : recipients) {
                // Nothing outstanding is nothing to instruct the bank to do.
                // Someone who earned nothing this month, or who has already
                // been settled, does not belong on a payment schedule.
                if (recipient.getOutstanding() <= 0) {
                    continue;
                }
                PayrollLineDTO line = new PayrollLineDTO();
                line.setUid(recipient.getUid());
                line.setRole(role);
                line.setName(recipient.getName());
                line.setEarned(recipient.getAmount());
                line.setPaid(recipient.getPaid());
                line.setToPay(recipient.getOutstanding());
                lines.add(line);
            }
        }

        attachContactDetails(lines);

        // Sorted within a role by what is owed, largest first: the lines that
        // matter most on a payment run are the ones read first.
        lines.sort((a, b) -> {
            int byRole = Integer.compare(ROLES.indexOf(a.getRole()), ROLES.indexOf(b.getRole()));
            return byRole != 0 ? byRole : Long.compare(b.getToPay(), a.getToPay());
        });

        RevenueShareDTO share = revenueShareService
                .findRevenueShare(period.getYear(), period.getMonthValue()).getData();

        PayrollDTO payroll = new PayrollDTO();
        payroll.setYear(period.getYear());
        payroll.setMonth(period.getMonthValue());
        payroll.setGeneratedAt(LocalDateTime.now());
        payroll.setRevenue(share == null ? 0 : share.getRevenue());
        payroll.setLines(lines);
        payroll.setRecipients(lines.size());
        payroll.setTotalToPay(lines.stream().mapToLong(PayrollLineDTO::getToPay).sum());
        payroll.setMissingPhone((int) lines.stream()
                .filter(line -> line.getPhone() == null || line.getPhone().isBlank())
                .count());

        return new Response<>(payroll);
    }

    /**
     * Phone numbers for everyone on the sheet, in one query rather than one
     * per line - a payroll is short, but this runs on a page that is opened
     * monthly by people who will not forgive a slow one.
     */
    private void attachContactDetails(List<PayrollLineDTO> lines) {
        if (lines.isEmpty()) {
            return;
        }
        List<String> uids = lines.stream().map(PayrollLineDTO::getUid).filter(java.util.Objects::nonNull).toList();
        Map<String, User> byUid = new HashMap<>();
        for (User user : userRepository.findAllById(uids)) {
            byUid.put(user.getUid(), user);
        }
        for (PayrollLineDTO line : lines) {
            User user = byUid.get(line.getUid());
            if (user != null) {
                line.setPhone(user.getPhone());
            }
        }
    }
}
