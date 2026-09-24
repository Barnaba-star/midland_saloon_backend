package com.midland.saloon.Payment.Controller;

import com.midland.saloon.Payment.Model.SubscriptionPayment;
import com.midland.saloon.Payment.Repository.SubscriptionPaymentRepository;
import com.midland.saloon.Payment.Service.PaymentReconcileService;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Utils.PageableParam;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import com.midland.saloon.Utils.Responses.ResponsePage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final SubscriptionPaymentRepository subscriptionPaymentRepository;
    private final PaymentReconcileService paymentReconcileService;

    /** Money in: every subscription payment that actually completed. */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_PAYMENTS')")
    @PostMapping("/findPaymentPage")
    public ResponsePage<SubscriptionPayment> findPaymentPage(@RequestBody PageableParam pageableParam) {
        return new ResponsePage<>(subscriptionPaymentRepository.findAll(pageableParam.pageable(false)));
    }

    /** Payments started and never resolved - the ones worth chasing. */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_PAYMENTS')")
    @GetMapping("/findUnresolvedPayments")
    public ResponseList<Branch> findUnresolvedPayments() {
        return paymentReconcileService.findUnresolvedPayments();
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('RECONCILE_PAYMENTS')")
    @PostMapping("/reconcile/{branchUID}")
    public Response<String> reconcile(@PathVariable String branchUID) {
        return paymentReconcileService.reconcile(branchUID);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_PAYMENTS')")
    @GetMapping("/findBalance")
    public Response<Long> findBalance() {
        return paymentReconcileService.findBalance();
    }
}
