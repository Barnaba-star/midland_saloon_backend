package com.midland.saloon.Setting.Controller;

import com.midland.saloon.Setting.Model.PlatformSetting;
import com.midland.saloon.Setting.Service.PlatformSettingService;
import com.midland.saloon.Utils.Responses.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/platformSetting")
@RequiredArgsConstructor
public class PlatformSettingController {

    private final PlatformSettingService platformSettingService;

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_PLATFORM_SETTING')")
    @GetMapping("/findPlatformSetting")
    public Response<PlatformSetting> findPlatformSetting() {
        return platformSettingService.findPlatformSetting();
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_PLATFORM_SETTING')")
    @PostMapping("/savePlatformSetting")
    public Response<PlatformSetting> savePlatformSetting(@RequestBody PlatformSetting platformSetting) {
        return platformSettingService.save(platformSetting);
    }
}
