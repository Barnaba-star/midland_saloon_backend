package com.midland.saloon.Setting.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Setting.Model.PlatformSetting;
import com.midland.saloon.Setting.Repository.PlatformSettingRepository;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Log
@RequiredArgsConstructor
public class PlatformSettingService {

    private final PlatformSettingRepository platformSettingRepository;

    /**
     * Read on every login and every commission calculation, so it is held in
     * memory rather than fetched each time. Written from one screen by one
     * role, so the only thing that can invalidate it is {@link #save}.
     */
    private volatile PlatformSetting cached;

    public PlatformSetting current() {
        PlatformSetting local = cached;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cached == null) {
                cached = platformSettingRepository.findFirstByIsActiveTrue()
                        // A brand new database has no row yet. The entity's
                        // field initialisers are the old hard-coded values, so
                        // this is the same behaviour as before, not a blank.
                        .orElseGet(PlatformSetting::new);
            }
            return cached;
        }
    }

    public Response<PlatformSetting> findPlatformSetting() {
        return new Response<>(current());
    }

    @Transactional
    public Response<PlatformSetting> save(PlatformSetting incoming) {
        if (incoming == null) {
            return new Response<>("Provide Platform Settings");
        }

        String invalid = validate(incoming);
        if (invalid != null) {
            return new Response<>(invalid);
        }

        // Always update the existing row - a second row would make
        // findFirstByIsActiveTrue non-deterministic.
        PlatformSetting setting = platformSettingRepository.findFirstByIsActiveTrue()
                .orElseGet(PlatformSetting::new);

        setting.setCommissionPercent(incoming.getCommissionPercent());
        setting.setTrialDays(incoming.getTrialDays());
        setting.setGracePeriodDays(incoming.getGracePeriodDays());
        setting.setMinimumPaymentAmount(incoming.getMinimumPaymentAmount());
        setting.setDefaultSubscriptionAmount(incoming.getDefaultSubscriptionAmount());
        setting.setDefaultSubscriptionDays(incoming.getDefaultSubscriptionDays());
        setting.setSessionHours(incoming.getSessionHours());
        setting.setErrorRetentionDays(incoming.getErrorRetentionDays());
        setting.setErrorPurgeDays(incoming.getErrorPurgeDays());
        setting.setAuditRetentionDays(incoming.getAuditRetentionDays());
        setting.update();

        PlatformSetting saved = platformSettingRepository.save(setting);
        cached = saved;
        log.info(LoggerUser.getEmail() + " updated platform settings");
        return new Response<>(saved);
    }

    /** Seeded once so the screen has a row to edit rather than an empty form. */
    @Transactional
    public void seedIfMissing() {
        if (platformSettingRepository.findFirstByIsActiveTrue().isEmpty()) {
            platformSettingRepository.save(new PlatformSetting());
            log.info("Seeded platform settings with default values");
        }
        cached = null;
    }

    private String validate(PlatformSetting s) {
        // A commission over 100% would pay out more than came in; a negative
        // one would take money off the staff member.
        if (outside(s.getCommissionPercent(), 0, 100)) {
            return "Commission percent must be between 0 and 100";
        }
        if (outside(s.getTrialDays(), 0, 365)) {
            return "Trial days must be between 0 and 365";
        }
        if (outside(s.getGracePeriodDays(), 0, 90)) {
            return "Grace period must be between 0 and 90 days";
        }
        if (outside(s.getMinimumPaymentAmount(), 1, 10_000_000)) {
            return "Minimum payment amount is not valid";
        }
        if (outside(s.getDefaultSubscriptionAmount(), 0, 100_000_000)) {
            return "Default subscription amount is not valid";
        }
        if (outside(s.getDefaultSubscriptionDays(), 1, 3650)) {
            return "Default subscription days must be between 1 and 3650";
        }
        // Below an hour nobody could finish a shift; a year-long token cannot
        // be revoked by expiry at all.
        if (outside(s.getSessionHours(), 1, 720)) {
            return "Session hours must be between 1 and 720";
        }
        if (outside(s.getErrorRetentionDays(), 1, 365)) {
            return "Error retention days must be between 1 and 365";
        }
        if (outside(s.getErrorPurgeDays(), 1, 3650)) {
            return "Error purge days must be between 1 and 3650";
        }
        if (outside(s.getAuditRetentionDays(), 30, 3650)) {
            return "Audit retention days must be between 30 and 3650";
        }
        // Purging before hiding would delete rows that are still on screen.
        if (s.getErrorPurgeDays() < s.getErrorRetentionDays()) {
            return "Error purge days must be greater than or equal to retention days";
        }
        return null;
    }

    private static boolean outside(Integer value, int min, int max) {
        return value == null || value < min || value > max;
    }
}
