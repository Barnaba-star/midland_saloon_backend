package com.midland.saloon.Saloon.Service;

import com.midland.saloon.Saloon.Dto.CashUpDTO;
import com.midland.saloon.Saloon.Model.CashUp;
import com.midland.saloon.Saloon.Model.CashUpLine;
import com.midland.saloon.Saloon.Repository.IncomeExpensesDescriptionRepository;
import com.midland.saloon.Saloon.Repository.CashUpLineRepository;
import com.midland.saloon.Saloon.Repository.CashUpRepository;
import com.midland.saloon.Saloon.Repository.SalesOpenedRepository;
import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Closing a shift. What a cashier should have in hand is the bills they
 * marked paid themselves (SalesOpened.paidBy) since their last cash-up - or since
 * the day began, for their very first one. Payouts they recorded in the system
 * in that time (a pot's Pay, staff commission, stock purchase) come off the
 * expected of the method each went by - read from those records, never typed at the cash-up. They
 * count it per method; the cash-up keeps both and the difference, as submitted.
 */
@Service
@RequiredArgsConstructor
public class CashUpService {

    /** Roles that see every cashier's cash-ups; anyone else sees only their own. */
    private static final Set<String> SEES_ALL = Set.of("ROOT", "DIRECTOR", "CEO", "MANAGER");

    private final CashUpRepository cashUpRepository;
    private final CashUpLineRepository lineRepository;
    private final SalesOpenedRepository salesOpenedRepository;
    private final IncomeExpensesDescriptionRepository payoutRepository;

    /** The open shift of whoever is signed in: since when, and what each method should hold. */
    public Response<Map<String, Object>> preview() {
        String branchUID = LoggerUser.getBranchUID();
        String email = LoggerUser.getEmail();
        LocalDateTime from = shiftStart(branchUID, email);
        LocalDateTime to = LocalDateTime.now();
        Shift shift = shift(branchUID, email, from, to);

        List<Map<String, Object>> lines = new ArrayList<>();
        for (String method : shift.methods()) {
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("method", method);
            line.put("takings", shift.takings(method));
            line.put("payouts", shift.payouts(method));
            line.put("expected", shift.expected(method));
            line.put("bills", shift.bills(method));
            lines.add(line);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cashierName", nameOf(LoggerUser.getUser()));
        body.put("from", from);
        body.put("to", to);
        body.put("lines", lines);
        body.put("payouts", shift.payoutRows);
        body.put("payoutsTotal", shift.payoutsTotal);
        body.put("expectedTotal", shift.expectedTotal());
        body.put("billCount", shift.billCount());
        return new Response<>(body);
    }

    /**
     * Closes the shift. Expected is worked out again here, at the moment of
     * closing, so it cannot be argued from the screen. Every method with
     * takings needs a count; cash may be counted even with none expected.
     */
    @Transactional
    public Response<CashUp> submit(CashUpDTO dto) {
        String branchUID = LoggerUser.getBranchUID();
        String email = LoggerUser.getEmail();
        if (branchUID == null || email == null)
            return new Response<>("Sign in again to close your shift");

        Map<String, Long> counted = new LinkedHashMap<>();
        if (dto != null && dto.getCounts() != null) {
            for (CashUpDTO.Count c : dto.getCounts()) {
                if (c.getMethod() == null || c.getCounted() == null)
                    continue;
                if (c.getCounted() < 0)
                    return new Response<>("A count cannot be below zero");
                counted.put(c.getMethod().trim().toLowerCase(), c.getCounted());
            }
        }

        LocalDateTime from = shiftStart(branchUID, email);
        LocalDateTime to = LocalDateTime.now();
        Shift shift = shift(branchUID, email, from, to);

        for (String method : shift.methods()) {
            boolean moved = shift.takings(method) != 0 || shift.payouts(method) != 0;
            if (moved && !counted.containsKey(method))
                return new Response<>("Enter what you counted for " + method);
        }
        if (shift.methods().isEmpty() && counted.isEmpty())
            return new Response<>("Nothing to close - no payments taken and nothing counted");

        CashUp cashUp = new CashUp();
        cashUp.setCashierEmail(email);
        cashUp.setCashierName(nameOf(LoggerUser.getUser()));
        cashUp.setPeriodFrom(from);
        cashUp.setPeriodTo(to);
        String note = dto == null || dto.getNote() == null ? null : dto.getNote().trim();
        cashUp.setNote(note == null || note.isEmpty() ? null : (note.length() > 500 ? note.substring(0, 500) : note));

        Set<String> methods = new TreeSet<>(shift.methods());
        methods.addAll(counted.keySet());
        long expectedTotal = 0, countedTotal = 0;
        int bills = 0;
        List<CashUpLine> lines = new ArrayList<>();
        for (String method : methods) {
            long exp = shift.expected(method);
            long cnt = counted.getOrDefault(method, 0L);
            CashUpLine line = new CashUpLine();
            line.setMethod(method);
            line.setTakings(shift.takings(method));
            line.setPayouts(shift.payouts(method));
            line.setExpected(exp);
            line.setCounted(cnt);
            line.setVariance(cnt - exp);
            line.setBills(shift.bills(method));
            lines.add(line);
            expectedTotal += exp;
            countedTotal += cnt;
            bills += shift.bills(method);
        }
        cashUp.setPayoutsTotal(shift.payoutsTotal);
        cashUp.setExpectedTotal(expectedTotal);
        cashUp.setCountedTotal(countedTotal);
        cashUp.setVariance(countedTotal - expectedTotal);
        cashUp.setBillCount(bills);
        CashUp saved = cashUpRepository.save(cashUp);
        for (CashUpLine line : lines) {
            line.setCashUp(saved);
            lineRepository.save(line);
        }
        return new Response<>(saved);
    }

    /** Cash-ups closed in a Reports period: all of them for managers, a cashier's own otherwise. */
    public ResponseList<CashUp> findClosed(String filter) {
        LocalDateTime[] range = ReportRange.of(filter);
        String email = seesAll() ? null : LoggerUser.getEmail();
        return new ResponseList<>(cashUpRepository.findClosed(LoggerUser.getBranchUID(), range[0], range[1], email));
    }

    public ResponseList<CashUpLine> findLines(String cashUpUid) {
        var cashUp = cashUpRepository.findInBranch(cashUpUid, LoggerUser.getBranchUID());
        if (cashUp.isEmpty() || (!seesAll() && !cashUp.get().getCashierEmail().equals(LoggerUser.getEmail())))
            return new ResponseList<>("Cash-up not found");
        return new ResponseList<>(lineRepository.findByCashUp(cashUpUid));
    }

    /** A shift's takings per method and the payouts recorded in it, each off the method it went by. */
    private Shift shift(String branchUID, String email, LocalDateTime from, LocalDateTime to) {
        Shift shift = new Shift();
        for (Object[] row : salesOpenedRepository.takingsOf(branchUID, email, from, to)) {
            shift.takings.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
            shift.bills.put(String.valueOf(row[0]), ((Number) row[2]).intValue());
        }
        for (Object[] row : payoutRepository.payoutsOf(branchUID, email, from, to)) {
            long amount = row[3] == null ? 0 : ((Number) row[3]).longValue();
            Map<String, Object> payout = new LinkedHashMap<>();
            payout.put("pot", row[0]);
            payout.put("description", row[1]);
            payout.put("paidTo", row[2]);
            payout.put("amount", amount);
            payout.put("paidAt", row[4]);
            // Payouts recorded before the method was kept went out in cash.
            String method = row[5] == null || String.valueOf(row[5]).isBlank() ? "cash" : String.valueOf(row[5]);
            payout.put("method", method);
            shift.payoutRows.add(payout);
            shift.payoutsTotal += amount;
            shift.payouts.merge(method, amount, Long::sum);
        }
        return shift;
    }

    private static final class Shift {
        final Map<String, Long> takings = new LinkedHashMap<>();
        final Map<String, Integer> bills = new LinkedHashMap<>();
        final Map<String, Long> payouts = new LinkedHashMap<>();
        final List<Map<String, Object>> payoutRows = new ArrayList<>();
        long payoutsTotal = 0;

        Set<String> methods() {
            Set<String> m = new TreeSet<>(takings.keySet());
            m.addAll(payouts.keySet());
            return m;
        }

        long takings(String method) { return takings.getOrDefault(method, 0L); }
        long payouts(String method) { return payouts.getOrDefault(method, 0L); }
        long expected(String method) { return takings(method) - payouts(method); }
        int bills(String method) { return bills.getOrDefault(method, 0); }
        long expectedTotal() { return methods().stream().mapToLong(this::expected).sum(); }
        int billCount() { return bills.values().stream().mapToInt(Integer::intValue).sum(); }
    }

    /**
     * Where the open shift began: the end of the cashier's last cash-up, whatever
     * day that was - a bar shift runs past midnight, and takings not yet cashed
     * up stay theirs to account for. Their first one starts at the day's start.
     */
    private LocalDateTime shiftStart(String branchUID, String email) {
        return cashUpRepository.lastClosedAt(branchUID, email)
                .orElse(LocalDate.now().atStartOfDay());
    }

    private static boolean seesAll() {
        User user = LoggerUser.getUser();
        if (user == null)
            return false;
        if (Boolean.TRUE.equals(user.getIsRoot()))
            return true;
        return user.getRoles() != null && user.getRoles().stream().anyMatch(r -> SEES_ALL.contains(r.getCode()));
    }

    private static String nameOf(User user) {
        if (user == null)
            return null;
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isEmpty() ? user.getUsername() : name;
    }
}
