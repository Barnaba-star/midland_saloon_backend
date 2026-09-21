package com.midland.saloon.Setting.Controller;

import com.midland.saloon.Setting.Dto.UserSettingDTo;
import com.midland.saloon.Uaa.Dto.AssignUserRoleDTO;
import com.midland.saloon.Uaa.Dto.UserAndAttachmentDTO;
import com.midland.saloon.Uaa.Dto.UserDTO;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Projection.UserProjection;
import com.midland.saloon.Uaa.Service.UserService;
import com.midland.saloon.Utils.PageableParam;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponsePage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/userSetting")
@RequiredArgsConstructor
public class UserSettingController {
    private final UserService userService;



    @PreAuthorize("@authChecker.hasPermissionOrRoot('DELETE_USER')")
    @PostMapping("/deleteUser/{userUID}")
    public Response<User> deleteUser(@PathVariable String userUID){
        return userService.deleteUser(userUID);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_USER')")
    @GetMapping("/findUserByUID/{userUID}")
    public Response<UserProjection> findUserByUID(@PathVariable String userUID){
        return userService.findUserByUID(userUID);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_USER')")
    @PostMapping("/findUserPAGE")
    public ResponsePage<User> findUserPAGE(@RequestBody PageableParam pageableParam){
        return userService.findUserPAGE(pageableParam.getPage(), pageableParam.getSize());
    }
    @PreAuthorize("@authChecker.hasPermissionOrRoot('VIEW_USER')")
    @PostMapping("/findUsers")
    public ResponsePage<UserProjection> findUsers(@RequestBody PageableParam pageableParam){
        return userService.findUsers(pageableParam.getPage(), pageableParam.getSize());
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('ASSIGN_USER_ROLE')")
    @PostMapping("/assignOrUnAssignUserRole")
    public Response<User> assignOrUnAssignUserRole(@RequestBody AssignUserRoleDTO assignUserRoleDTO){
        return userService.assignOrUnAssignUserRole(assignUserRoleDTO);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('ENABLE_OR_DISABLE_ACCOUNT')")
    @PostMapping("/enableOrDisableAccount/{userUID}/{enable}")
    public Response<User> enableOrDisableAccount(@PathVariable String userUID, @PathVariable Boolean enable){
        return userService.enableOrDisableAccount(userUID, enable);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_USER')")
    @PostMapping("/saveUser")
    public Response<User> saveUser(@ RequestBody  UserDTO userDTO){
        return userService.saveUser(userDTO);
    }

}
