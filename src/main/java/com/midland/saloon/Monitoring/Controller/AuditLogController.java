package com.midland.saloon.Monitoring.Controller;

import com.midland.saloon.Monitoring.Model.AuditLog;
import com.midland.saloon.Monitoring.Service.AuditLogService;
import com.midland.saloon.Utils.PageableParam;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponsePage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Read-only on purpose. There is no endpoint to clear this: a trail that can
 * be tidied away from the screen is worth less than one that cannot.
 */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_AUDIT_LOG')")
    @PostMapping("/findAuditPage")
    public ResponsePage<AuditLog> findAuditPage(
            @RequestBody PageableParam pageableParam,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String outcome
    ) {
        return auditLogService.findAuditPage(pageableParam, username, outcome);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_AUDIT_LOG')")
    @GetMapping("/findAuditSummary")
    public Response<Map<String, Long>> findAuditSummary() {
        return auditLogService.findAuditSummary();
    }
}
