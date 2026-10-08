package com.midland.saloon.Saloon.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Saloon.Model.CashUp;
import com.midland.saloon.Saloon.Model.StockTake;
import com.midland.saloon.Saloon.Model.WorkShift;
import com.midland.saloon.Saloon.Repository.CashUpRepository;
import com.midland.saloon.Saloon.Repository.StockTakeRepository;
import com.midland.saloon.Saloon.Repository.WorkShiftRepository;
import com.midland.saloon.Setting.Model.Role;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Utils.Exceptions.BusinessException;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Opening and closing a cashier's shift (zamu). Whoever takes payments opens
 * one first; bills are opened, added to and paid only inside it. Closing it
 * fixes the window its cash-up counts (opened..closed), the store count taken
 * then is recorded against it, and the next one can't be opened until the
 * cash-up is done - so money short and store items missing stay on the shift
 * of whoever opened it.
 */
@Service
@RequiredArgsConstructor
public class WorkShiftService {

    /** Roles above the till: their payouts are not drawer money, so they need no shift for those. */
    private static final Set<String> ABOVE_TILL = Set.of("ROOT", "DIRECTOR", "STAFF", "CEO", "MANAGER");
    /** Roles that see every cashier's shifts; anyone else sees only their own. */
    private static final Set<String> SEES_ALL = Set.of("ROOT", "DIRECTOR", "CEO", "MANAGER");

    private final WorkShiftRepository shiftRepository;
    private final CashUpRepository cashUpRepository;
    private final StockTakeRepository stockTakeRepository;

    /**
     * The signed-in login's shift: NONE, OPEN, or CLOSED (waiting for its
     * cash-up). The CEO and manager also get `others`: shifts other people
     * have open in the branch right now - for seeing only; to sell they open
     * their own.
     */
    public Response<Map<String, Object>> current() {
        String branchUID = LoggerUser.getBranchUID();
        String email = LoggerUser.getEmail();
        Map<String, Object> body = new LinkedHashMap<>();
        Optional<WorkShift> shift = unfinished(branchUID, email);
        body.put("state", shift.map(WorkShift::getStatus).orElse("NONE"));
        body.put("shift", shift.orElse(null));
        List<Map<String, Object>> others = new ArrayList<>();
        if (branchUID != null && seesAll()) {
            for (WorkShift s : shiftRepository.findAllUnfinished(branchUID, null)) {
                if (!WorkShift.OPEN.equals(s.getStatus()) || s.getCashierEmail().equals(email))
                    continue;
                Map<String, Object> other = new LinkedHashMap<>();
                other.put("cashierName", s.getCashierName() != null ? s.getCashierName() : s.getCashierEmail());
                other.put("openedAt", s.getOpenedAt());
                others.add(other);
            }
        }
        body.put("others", others);
        return new Response<>(body);
    }

    @Transactional
    public Response<WorkShift> open() {
        String branchUID = LoggerUser.getBranchUID();
        String email = LoggerUser.getEmail();
        if (branchUID == null || email == null)
            return new Response<>("Sign in again to open your shift");
        Optional<WorkShift> existing = unfinished(branchUID, email);
        if (existing.isPresent()) {
            return WorkShift.OPEN.equals(existing.get().getStatus())
                    ? new Response<>("Your shift is already open")
                    : new Response<>("Do the cash-up of your closed shift before opening a new one");
        }
        WorkShift shift = new WorkShift();
        shift.setCashierEmail(email);
        shift.setCashierName(nameOf(LoggerUser.getUser()));
        LocalDateTime openedAt = LocalDateTime.now();
        // Never before the last shift ended: two shifts must not share a payment.
        LocalDateTime lastEnd = shiftRepository.lastClosedAt(branchUID, email).orElse(null);
        if (lastEnd != null && openedAt.isBefore(lastEnd))
            openedAt = lastEnd;
        shift.setOpenedAt(openedAt);
        shift.setStatus(WorkShift.OPEN);
        return new Response<>(shiftRepository.save(shift));
    }

    @Transactional
    public Response<WorkShift> close() {
        Optional<WorkShift> shift = unfinished(LoggerUser.getBranchUID(), LoggerUser.getEmail())
                .filter(s -> WorkShift.OPEN.equals(s.getStatus()));
        if (shift.isEmpty())
            return new Response<>("You have no open shift to close");
        WorkShift s = shift.get();
        s.setClosedAt(LocalDateTime.now());
        s.setStatus(WorkShift.CLOSED);
        s.update();
        return new Response<>(shiftRepository.save(s));
    }

    /** Selling - opening a bill, adding to it, taking payment - needs the login's shift open. */
    public void requireOpen() {
        Optional<WorkShift> shift = unfinished(LoggerUser.getBranchUID(), LoggerUser.getEmail());
        if (shift.isEmpty())
            throw new BusinessException("Fungua zamu yako kwanza kabla ya kuuza (Open your shift before selling)");
        if (!WorkShift.OPEN.equals(shift.get().getStatus()))
            throw new BusinessException("Zamu yako imefungwa - fanya makabidhiano kisha fungua zamu mpya (Your shift is closed - do its cash-up and open a new one)");
    }

    /**
     * Paying out of the drawer (a pot, a commission, a stock purchase) is a
     * cashier's job and comes off their cash-up, so it has to fall inside
     * their shift. CEO and above pay from wherever the money is; they need none.
     */
    public void requireOpenForPayout() {
        User user = LoggerUser.getUser();
        boolean aboveTill = user != null && (Boolean.TRUE.equals(user.getIsRoot())
                || (user.getRoles() != null && user.getRoles().stream().map(Role::getCode).anyMatch(ABOVE_TILL::contains)));
        if (!aboveTill)
            requireOpen();
    }

    /** The closed shift waiting for this login's cash-up, if there is one. */
    public Optional<WorkShift> awaitingCashUp(String branchUID, String email) {
        return unfinished(branchUID, email).filter(s -> WorkShift.CLOSED.equals(s.getStatus()));
    }

    /**
     * The shift a store count belongs to: this login's open one, the closed
     * one awaiting cash-up, or - counted right after the cash-up - the one
     * closed in the last few hours. The handover order doesn't matter.
     */
    public Optional<WorkShift> currentForCount(String branchUID, String email) {
        Optional<WorkShift> live = unfinished(branchUID, email);
        if (live.isPresent() || branchUID == null || email == null)
            return live;
        return shiftRepository.findClosedSince(branchUID, email, LocalDateTime.now().minusHours(3)).stream().findFirst();
    }

    void markCashedUp(WorkShift shift, CashUp cashUp) {
        shift.setStatus(WorkShift.CASHED_UP);
        shift.setCashUpUid(cashUp.getUid());
        shift.update();
        shiftRepository.save(shift);
    }

    /**
     * Shifts for the CEO and manager to look over: those opened in the period
     * plus any still open or waiting for cash-up. A cashier sees their own.
     * Each row carries its money variance (cash-up) and store loss (counts).
     */
    public ResponseList<Map<String, Object>> list(String filter) {
        String branchUID = LoggerUser.getBranchUID();
        String email = seesAll() ? null : LoggerUser.getEmail();
        LocalDateTime[] range = ReportRange.of(filter);
        Map<String, WorkShift> shifts = new LinkedHashMap<>();
        for (WorkShift s : shiftRepository.findAllUnfinished(branchUID, email))
            shifts.put(s.getUid(), s);
        for (WorkShift s : shiftRepository.findOpenedIn(branchUID, range[0], range[1], email))
            shifts.putIfAbsent(s.getUid(), s);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (WorkShift s : shifts.values()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("uid", s.getUid());
            row.put("cashierName", s.getCashierName());
            row.put("cashierEmail", s.getCashierEmail());
            row.put("openedAt", s.getOpenedAt());
            row.put("closedAt", s.getClosedAt());
            row.put("status", s.getStatus());
            LocalDateTime end = s.getClosedAt() != null ? s.getClosedAt() : LocalDateTime.now();
            row.put("minutes", Duration.between(s.getOpenedAt(), end).toMinutes());
            CashUp cashUp = s.getCashUpUid() == null ? null
                    : cashUpRepository.findInBranch(s.getCashUpUid(), branchUID).orElse(null);
            row.put("cashUpUid", s.getCashUpUid());
            row.put("expectedTotal", cashUp == null ? null : cashUp.getExpectedTotal());
            row.put("countedTotal", cashUp == null ? null : cashUp.getCountedTotal());
            row.put("variance", cashUp == null ? null : cashUp.getVariance());
            List<StockTake> counts = stockTakeRepository.findByShift(s.getUid());
            row.put("storeCounts", counts.size());
            row.put("storeLoss", counts.isEmpty() ? null
                    : counts.stream().mapToLong(t -> t.getLossValue() == null ? 0 : t.getLossValue()).sum());
            row.put("storeGain", counts.isEmpty() ? null
                    : counts.stream().mapToLong(t -> t.getGainValue() == null ? 0 : t.getGainValue()).sum());
            rows.add(row);
        }
        return new ResponseList<>(rows);
    }

    private Optional<WorkShift> unfinished(String branchUID, String email) {
        if (branchUID == null || email == null)
            return Optional.empty();
        return shiftRepository.findUnfinished(branchUID, email).stream().findFirst();
    }

    static boolean seesAll() {
        User user = LoggerUser.getUser();
        if (user == null)
            return false;
        if (Boolean.TRUE.equals(user.getIsRoot()))
            return true;
        return user.getRoles() != null && user.getRoles().stream().anyMatch(r -> SEES_ALL.contains(r.getCode()));
    }

    static String nameOf(User user) {
        if (user == null)
            return null;
        String name = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return name.isEmpty() ? user.getUsername() : name;
    }
}
