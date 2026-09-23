package com.midland.saloon.Setting.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Payment.Client.SnippeClient;
import com.midland.saloon.Payment.Dto.SnippePaymentResult;
import com.midland.saloon.Setting.Dto.BranchDTO;
import com.midland.saloon.Setting.Dto.SubscriptionPaymentDTO;
import com.midland.saloon.Setting.Dto.TableSizeDto;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Setting.Repository.DatabaseMonitoringRepository;
import com.midland.saloon.Uaa.Model.User;
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
    private final BranchService branchService;
    private final SnippeClient snippeClient;
    private final PlatformSettingService platformSettingService;

    public SettingService(DatabaseMonitoringRepository repository, BranchRepository branchRepository, SnippeClient snippeClient, BranchService branchService, PlatformSettingService platformSettingService) {
        this.repository = repository;
        this.branchRepository = branchRepository;
        this.snippeClient = snippeClient;
        this.branchService = branchService;
        this.platformSettingService = platformSettingService;
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
        // STAFF may only touch branches they registered themselves.
        if(!branchService.canTouch(optionalBranch.get()))
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
    /**
     * Branch user requests a subscription payment: we work out how much they
     * owe from the branch's configured subscriptionAmount (set by an admin
     * via saveBranchSubscription) times the number of months they picked,
     * then hand off to Snippe to trigger a mobile money USSD push.
     *
     * This only ever returns a "pending" outcome - Snippe confirms the real
     * result (payment.completed / payment.failed) via webhook, which is
     * where the subscription actually gets extended
     * (see SnippeWebhookController). We never mark the branch as paid here,
     * since the customer hasn't authorised anything yet at this point.
     */
    // Mirrored in subscribe-dialog-component.ts, which checks the same total
    // before sending so the provider's own message never reaches a customer.

    public Response<Branch> updateSubscription(SubscriptionPaymentDTO subscriptionPaymentDTO){
        log.info(LoggerUser.getEmail() + "Is Requesting Subscription Payment");

        if(subscriptionPaymentDTO == null)
            return new Response<>("Provide Subscription Payment Details");
        if(subscriptionPaymentDTO.getMonths() == null || subscriptionPaymentDTO.getMonths() <= 0)
            return new Response<>("Provide A Valid Number Of Months");
        if(LoggerUser.getBranchUID() == null)
            return new Response<>("User Logged In Yet have No Branch");

        Optional<Branch> optionalBranch = branchRepository.findById(Objects.requireNonNull(LoggerUser.getBranchUID()));
        if(optionalBranch.isEmpty())
            return new Response<>("Branch Not Found");

        Branch branch = optionalBranch.get();

        if(branch.getSubscriptionAmount() == null || branch.getSubscriptionAmount() <= 0)
            return new Response<>("This Branch Has No Subscription Plan Configured Yet, Contact Admin");

        int amount = branch.getSubscriptionAmount() * subscriptionPaymentDTO.getMonths();

        // Snippe refuses anything under this and answers with its own raw
        // English text. The dialog already blocks it client-side (it knows the
        // branch's monthly amount), so this only catches a request made
        // outside the UI - but the provider's wording must never be what a
        // caller sees, so stop here rather than forwarding it.
        int minimumPaymentAmount = platformSettingService.current().getMinimumPaymentAmount();
        if (amount < minimumPaymentAmount)
            return new Response<>("The Total Is Below The Minimum Of " + minimumPaymentAmount + " TZS, Choose More Months");

        User user = LoggerUser.getUser();

        SnippePaymentResult result = snippeClient.createMobilePayment(
                amount,
                subscriptionPaymentDTO.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                branch.getUid(),
                subscriptionPaymentDTO.getMonths()
        );

        if(!result.isSuccess())
            return new Response<>(result.getMessage() != null ? result.getMessage() : "Failed To Initiate Payment");

        branch.setSubscriptionStatus("PENDING");
        branch.setSubscriptionPhoneNumber(subscriptionPaymentDTO.getPhoneNumber());

        try {
            branch = branchRepository.save(branch);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response<>("Payment Was Initiated But Failed To Update Branch, Contact Admin");
        }

        // No message on this success response on purpose - StatusInterceptor
        // (frontend) pops up an "info" dialog for any response carrying a
        // message, which duplicated SubscribeDialogComponent's own success
        // banner and looked like an error interrupting the flow. The dialog
        // already shows its own confirmation text.
        return new Response<>(branch);
    }
}
