package com.midland.saloon.Setting.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Setting.Dto.BranchDTO;
import com.midland.saloon.Setting.Dto.TableSizeDto;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Setting.Repository.DatabaseMonitoringRepository;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
@Log
public class SettingService {

    private final DatabaseMonitoringRepository repository;
    private final BranchRepository branchRepository;

    public SettingService(DatabaseMonitoringRepository repository, BranchRepository branchRepository) {
        this.repository = repository;
        this.branchRepository = branchRepository;
    }

    public ResponseList<TableSizeDto> getTableSizes() {
        return new ResponseList<>(repository.findTableSizes());
    }

    public Response<Branch> saveBranchSubscription(BranchDTO branchDTO){
        log.info(LoggerUser.getEmail() + "Is Saving Branch Subscription");
        if(branchDTO == null)
            return new Response<>("Provide Data For Branch Subscription");
        if(branchDTO.getCloseSubscription() == null)
            return new Response<>("Provide Subscription End Date");
        if(branchDTO.getSubscriptionAmount() == null)
            return new Response<>("Provide Amount For Subscription");
        if(branchDTO.getUid() == null)
            return new Response<>("Provide Branch REF");
        Optional<Branch> optionalBranch = branchRepository.findById(branchDTO.getUid());
        if(optionalBranch.isEmpty())
            return new Response<>("Branch Not Found");
        Branch branch = optionalBranch.get();
        branch.setCloseSubscription(branchDTO.getCloseSubscription());
        branch.setOpenSubscription(LocalDate.now());
        branch.setSubscriptionAmount(branchDTO.getSubscriptionAmount());
        branch.setSubscriptionStatus("FREE");
        try{
            return new Response<>(branchRepository.save(branch));
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>("Error When Saving Branch Subscription");
        }
    }
    public Response<Branch> updatingBranchSubscription(BranchDTO branchDTO){
        log.info(LoggerUser.getEmail() + "Is Updating Branch");
        if(branchDTO == null)
            return new Response<>("provide Data For Updating Branch");
        if(LoggerUser.getBranchUID() == null)
            return new Response<>("User Logged In Yet have No Branch");
        Optional<Branch> optionalBranch = branchRepository.findById(Objects.requireNonNull(LoggerUser.getBranchUID()));
        if(optionalBranch.isEmpty())
            return new Response<>("Branch Not Found");
        Branch branch = optionalBranch.get();
        Integer days = branchDTO.getSubscriptionDays();
        Integer amount = branch.getSubscriptionAmount();
        Integer calculatedAmount = days * amount;

        //CALL FOR METHOD PAYMENTS
        return null;

    }
}
