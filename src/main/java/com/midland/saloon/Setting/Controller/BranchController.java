package com.midland.saloon.Setting.Controller;

import com.midland.saloon.Setting.Dto.BranchDTO;
import com.midland.saloon.Setting.Dto.ExpiringBranchDTO;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Projection.BranchProjection;
import com.midland.saloon.Setting.Service.BranchDataPurgeService;
import com.midland.saloon.Setting.Service.BranchPeriodPurgeService;
import com.midland.saloon.Setting.Service.BranchService;
import java.util.Map;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Utils.PageableParam;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import com.midland.saloon.Utils.Responses.ResponsePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/branch")
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;
    private final BranchDataPurgeService branchDataPurgeService;
    private final BranchPeriodPurgeService branchPeriodPurgeService;

    /**
     * Wipes a branch's working data (sales, services, stylists, store,
     * reports...) and keeps the branch, its users and its subscription.
     * ROOT only; the body must carry the branch code typed back as
     * {"confirmCode": "..."}. The service checks both again.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('ROOT')")
    @PostMapping("/purgeBranchData/{branchUID}")
    public Response<Map<String, Integer>> purgeBranchData(@PathVariable String branchUID,
                                                          @RequestBody Map<String, String> body) {
        return branchDataPurgeService.purge(branchUID, body.get("confirmCode"));
    }

    /**
     * Clears a branch's records dated from..to (yyyy-MM-dd, both included).
     * ROOT only. {"from", "to", "dryRun": true} counts what would go;
     * the real run also needs {"confirmCode": "<branch code>"}.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('ROOT')")
    @PostMapping("/purgeBranchPeriod/{branchUID}")
    public Response<Map<String, Integer>> purgeBranchPeriod(@PathVariable String branchUID,
                                                            @RequestBody Map<String, Object> body) {
        return branchPeriodPurgeService.purge(branchUID,
                parseDay(body.get("from")), parseDay(body.get("to")),
                body.get("confirmCode") == null ? null : body.get("confirmCode").toString(),
                Boolean.TRUE.equals(body.get("dryRun")));
    }

    private static java.time.LocalDate parseDay(Object value) {
        try {
            return value == null ? null : java.time.LocalDate.parse(value.toString().trim());
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_BRANCH')")
    @PostMapping("/saveBranch")
    public Response<Branch> saveBranch(@Valid @RequestBody BranchDTO branchDTO){
        return branchService.saveBranch(branchDTO);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_BRANCH')")
    @GetMapping ("/findBranchByUID/{branchUID}")
    public Response<Branch> findBranchByUID(@PathVariable String branchUID){
        return branchService.findBranchByUID(branchUID);
    }

    // Cross-branch admin views (every branch, not just your own) - ROOT-only
    // in practice, since VIEW_ALL_BRANCHES is never assigned to CEO/MANAGER/
    // CASHIER roles. Kept separate from VIEW_BRANCH, which every role has
    // just to see their own branch's name/subscription in the header.
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_ALL_BRANCHES')")
    @PostMapping ("/findBranchPage")
    public ResponsePage<Branch> findBranchPage(@RequestBody PageableParam pageableParam){
        return branchService.findBranchPage(pageableParam);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_BRANCH')")
    @PostMapping ("/deleteBranch/{branchUID}")
    public Response<Branch> deleteBranch(@PathVariable String branchUID){
        return branchService.deleteBranch(branchUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_ALL_BRANCHES')")
    @GetMapping ("/findBranchList")
    public ResponseList<BranchProjection> findBranchList(){
        return branchService.findBranchList();
    }


    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_ALL_BRANCHES')")
    @GetMapping ("/findAllUsersWithBranchAndRoles/{branchUID}")
    public ResponseList<User> findAllUsersWithBranchAndRoles(@PathVariable String branchUID){
        return branchService.findAllUsersWithBranchAndRoles(branchUID);
    }

    /**
     * The branches worth chasing: running out soon, or already lapsed.
     * Same permission as the branch list, and narrowed the same way.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_ALL_BRANCHES')")
    @GetMapping("/findExpiringBranches")
    public ResponseList<ExpiringBranchDTO> findExpiringBranches(@RequestParam(required = false) Integer days) {
        return branchService.findExpiringBranches(days);
    }
}
