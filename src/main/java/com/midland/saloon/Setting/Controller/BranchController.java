package com.midland.saloon.Setting.Controller;

import com.midland.saloon.Setting.Dto.BranchDTO;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Projection.BranchProjection;
import com.midland.saloon.Setting.Service.BranchService;
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

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_BRANCH')")
    @PostMapping ("/findBranchPage")
    public ResponsePage<Branch> findBranchPage(@RequestBody PageableParam pageableParam){
        return branchService.findBranchPage(pageableParam.getPage(), pageableParam.getSize());
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_BRANCH')")
    @PostMapping ("/deleteBranch/{branchUID}")
    public Response<Branch> deleteBranch(@PathVariable String branchUID){
        return branchService.deleteBranch(branchUID);
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_BRANCH')")
    @GetMapping ("/findBranchList")
    public ResponseList<BranchProjection> findBranchList(){
        return branchService.findBranchList();
    }


    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_BRANCH')")
    @GetMapping ("/findAllUsersWithBranchAndRoles/{branchUID}")
    public ResponseList<User> findAllUsersWithBranchAndRoles(@PathVariable String branchUID){
        return branchService.findAllUsersWithBranchAndRoles(branchUID);
    }
}
