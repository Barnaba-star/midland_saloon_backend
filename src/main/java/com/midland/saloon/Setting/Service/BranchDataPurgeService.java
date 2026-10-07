package com.midland.saloon.Setting.Service;

import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Config.Security.AuthChecker;
import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Wipes one branch's working data so it can start again clean: sales and
 * bills, services, stylists, store and stock, commissions, reports, cash-ups,
 * income and expenses. The branch itself, its users and roles, its
 * subscription payments and the audit/error trail stay.
 *
 * ROOT only, never the main (ROOT) branch, and only when the caller types the
 * branch's code back - this cannot be undone. Everything runs in one
 * transaction: it all goes, or nothing does.
 */
@Service
@Log
@RequiredArgsConstructor
public class BranchDataPurgeService {

    private static final String ROOT_BRANCH_CODE = "ROOT";

    /**
     * Children before parents, so no foreign key is ever left pointing at a
     * deleted row. Line tables also go by their parent, in case an old line
     * was saved without its branch.
     */
    private static final List<String[]> STEPS = List.of(
            new String[]{"saloon_reports", null},
            new String[]{"service_store_report", null},
            new String[]{"saloon_sales", null},
            new String[]{"sales_opened", null},
            new String[]{"staff_commissions", null},
            new String[]{"stock_and_purchase_descriptions", "stock_and_purchase IN (SELECT uid FROM stock_and_purchase WHERE branch_uid = ?)"},
            new String[]{"stock_and_purchase", null},
            new String[]{"stock_take_lines", "stock_take_uid IN (SELECT uid FROM stock_takes WHERE branch_uid = ?)"},
            new String[]{"stock_takes", null},
            new String[]{"cash_up_lines", "cash_up_uid IN (SELECT uid FROM cash_ups WHERE branch_uid = ?)"},
            new String[]{"cash_ups", null},
            new String[]{"income_expenses_description", "income_expenses_uid IN (SELECT uid FROM income_expenses WHERE branch_uid = ?)"},
            new String[]{"income_expenses", null},
            new String[]{"other_commission_entries", null},
            new String[]{"other_commission_items", null},
            new String[]{"saloon_staffs", null},
            new String[]{"store_open", null},
            new String[]{"commissions", null},
            new String[]{"store", null},
            new String[]{"saloon_services", null}
    );

    private final JdbcTemplate jdbc;
    private final BranchRepository branchRepository;
    private final AuthChecker authChecker;

    @Transactional
    public Response<Map<String, Integer>> purge(String branchUID, String confirmCode) {
        // ROOT itself, not a permission a role could be handed.
        if (!Boolean.TRUE.equals(authChecker.hasPermissionOrRoot("ROOT"))) {
            return new Response<>("Only ROOT can wipe a branch's data");
        }
        Branch branch = branchRepository.findById(branchUID).orElse(null);
        if (branch == null) {
            return new Response<>("Branch Not Found");
        }
        if (ROOT_BRANCH_CODE.equalsIgnoreCase(branch.getBranchCode())) {
            return new Response<>("The main branch's data cannot be wiped");
        }
        if (confirmCode == null || !confirmCode.trim().equalsIgnoreCase(branch.getBranchCode())) {
            return new Response<>("Type the branch code exactly to confirm");
        }

        log.warning(LoggerUser.getEmail() + " is wiping all data of branch " + branch.getBranchCode());
        Set<String> tables = existingTables();
        Map<String, Integer> deleted = new LinkedHashMap<>();
        for (String[] step : STEPS) {
            String table = step[0];
            if (!tables.contains(table)) {
                continue;
            }
            int n = 0;
            if (step[1] != null) {
                n += jdbc.update("DELETE FROM " + table + " WHERE " + step[1], branchUID);
            }
            n += jdbc.update("DELETE FROM " + table + " WHERE branch_uid = ?", branchUID);
            if (n > 0) {
                deleted.put(table, n);
            }
        }
        log.warning("Branch " + branch.getBranchCode() + " wiped: " + deleted);
        return new Response<>(deleted);
    }

    /** Tables that exist and carry a branch_uid - older databases may lack some. */
    private Set<String> existingTables() {
        return new HashSet<>(jdbc.queryForList(
                "SELECT table_name FROM information_schema.columns "
                        + "WHERE table_schema = current_schema() AND column_name = 'branch_uid'",
                String.class));
    }
}
