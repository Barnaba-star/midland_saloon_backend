package com.midland.saloon.Setting.Controller;

import com.midland.saloon.Setting.Dto.UserSettingDTo;
import com.midland.saloon.Uaa.Dto.AssignUserRoleDTO;
import com.midland.saloon.Uaa.Dto.UserAndAttachmentDTO;
import com.midland.saloon.Uaa.Dto.BankDetailsDTO;
import com.midland.saloon.Uaa.Dto.SavedUserDTO;
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
        return userService.findUsers(pageableParam);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('ASSIGN_USER_ROLE')")
    @PostMapping("/assignOrUnAssignUserRole")
    public Response<User> assignOrUnAssignUserRole(@RequestBody AssignUserRoleDTO assignUserRoleDTO){
        return userService.assignOrUnAssignUserRole(assignUserRoleDTO);
    }

    /**
     * Revokes somebody's access, or gives it back. The path says `blocked`
     * because that is the column it sets - the old name said `enable` and
     * meant the opposite.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('ENABLE_OR_DISABLE_ACCOUNT')")
    @PostMapping("/setAccountBlocked/{userUID}/{blocked}")
    public Response<String> setAccountBlocked(@PathVariable String userUID, @PathVariable Boolean blocked){
        return userService.setAccountBlocked(userUID, blocked);
    }

    /**
     * Issues a new one-time code and texts it. For the account that never got
     * the first message, let it expire, or burned it on wrong guesses.
     */
    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_USER')")
    @PostMapping("/resendActivationCode/{userUID}")
    public Response<String> resendActivationCode(@PathVariable String userUID){
        return userService.resendActivationCode(userUID);
    }

    /**
     * The caller's own bank details. findUserByUID needs VIEW_USER, which a
     * cashier does not have - so reading your own would have been refused on
     * the way into your own profile.
     */
    @GetMapping("/findMyBankDetails")
    public Response<BankDetailsDTO> findMyBankDetails(){
        return userService.findMyBankDetails();
    }

    /**
     * Deliberately ungated: anyone signed in may set their own bank details,
     * and the service refuses somebody else's without SAVE_USER. A
     * @PreAuthorize here would stop a staff member filling in their own.
     */
    @PostMapping("/saveBankDetails/{userUID}")
    public Response<String> saveBankDetails(@PathVariable String userUID,
                                            @RequestBody BankDetailsDTO dto){
        return userService.saveBankDetails(userUID, dto);
    }

    @PreAuthorize("@authChecker.hasPermissionOrRoot('SAVE_USER')")
    @PostMapping("/saveUser")
    public Response<SavedUserDTO> saveUser(@ RequestBody  UserDTO userDTO){
        return userService.saveUser(userDTO);
    }

}
