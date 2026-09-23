package com.midland.saloon.Payment.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.midland.saloon.Payment.Client.SnippeClient;
import com.midland.saloon.Payment.Service.CommissionService;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Receives payment.completed / payment.failed / payment.expired /
 * payment.voided events from Snippe for branch subscription payments
 * started in SettingService.updateSubscription(). This is the ONLY place
 * a subscription actually gets extended - the initial request only ever
 * puts the branch in PENDING, since the customer hasn't authorised the
 * USSD push yet at that point.
 *
 * Must stay publicly reachable (no JWT) - see WebSecurityConfiguration,
 * "/setting/webhooks/**" is permitAll. Trust is established purely via
 * HMAC signature verification below, not authentication.
 */
@RestController
@RequestMapping("/setting/webhooks")
@RequiredArgsConstructor
@Log
public class SnippeWebhookController {

    private final SnippeClient snippeClient;
    private final BranchRepository branchRepository;
    private final CommissionService commissionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/snippe")
    public ResponseEntity<String> handleSnippeWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Webhook-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Webhook-Signature", required = false) String signature
    ) {
        if (!snippeClient.verifyWebhookSignature(rawBody, timestamp, signature)) {
            log.warning("Rejected Snippe webhook: invalid or missing signature");
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        try {
            JsonNode event = objectMapper.readTree(rawBody);
            String type = event.path("type").asText();
            JsonNode data = event.path("data");
            String reference = data.path("reference").asText(null);
            String branchUID = data.path("metadata").path("branchUID").asText(null);

            if (branchUID == null) {
                return ResponseEntity.ok("OK");
            }

            switch (type) {
                case "payment.completed" -> applyCompletedPayment(branchUID, reference, data);
                case "payment.failed", "payment.expired", "payment.voided" -> markSubscriptionFailed(branchUID);
                default -> log.info("Ignoring unhandled Snippe webhook event type: " + type);
            }

        } catch (Exception e) {
            // Log and still return 200 - a payload we can't parse will never
            // succeed on retry either, so let it drop instead of triggering
            // Snippe's retry schedule for nothing.
            log.warning("Failed to process Snippe webhook: " + e.getMessage());
        }

        return ResponseEntity.ok("OK");
    }

    private void applyCompletedPayment(String branchUID, String reference, JsonNode data) {
        branchRepository.findById(branchUID).ifPresent(branch -> {

            if (reference != null && reference.equals(branch.getLastSubscriptionPaymentRef())) {
                // Already applied this exact payment - Snippe redelivered the event.
                return;
            }

            int months = parseMonths(data);
            if (months <= 0) {
                log.warning("payment.completed for branch " + branchUID + " missing/invalid metadata.months");
                return;
            }

            // Record the payment BEFORE overwriting the branch: these columns
            // only ever hold the current state, so once they're rewritten the
            // previous payment is gone. The history lives in
            // subscription_payments and is what staff commission is paid from.
            commissionService.recordPayment(branch, reference, parseAmount(data), months);

            LocalDate base = branch.getCloseSubscription() != null && branch.getCloseSubscription().isAfter(LocalDate.now())
                    ? branch.getCloseSubscription()
                    : LocalDate.now();

            branch.setOpenSubscription(LocalDate.now());
            branch.setCloseSubscription(base.plusMonths(months));
            branch.setSubscriptionStatus("ACTIVE");
            branch.setLastSubscriptionPaymentRef(reference);

            branchRepository.save(branch);

            log.info("Extended subscription for branch " + branchUID + " by " + months + " month(s)");
        });
    }

    private void markSubscriptionFailed(String branchUID) {
        branchRepository.findById(branchUID).ifPresent(branch -> {
            branch.setSubscriptionStatus("FAILED");
            branchRepository.save(branch);
        });
    }

    // Snippe echoes back the amount we charged. Null rather than 0 when it's
    // absent, so recordPayment can tell "not reported" from "free" and fall
    // back to the branch's own rate instead of filing a zero.
    private Integer parseAmount(JsonNode data) {
        JsonNode amount = data.path("amount");
        return amount.isNumber() ? amount.asInt() : null;
    }

    private int parseMonths(JsonNode data) {
        try {
            return Integer.parseInt(data.path("metadata").path("months").asText("0"));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
