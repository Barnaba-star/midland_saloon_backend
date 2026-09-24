package com.midland.saloon.Uaa.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Model.Role;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Setting.Repository.RoleRepository;
import com.midland.saloon.Uaa.Dto.*;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Projection.UserProjection;
import com.midland.saloon.Notification.Sms.Service.SmsService;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Utils.Exceptions.BusinessException;
import com.midland.saloon.Utils.PageableParam;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponsePage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final SmsService smsService;


    public Response<User> saveUser(UserDTO userDTO){
        log.info(LoggerUser.getEmail() + "Is Saving User");
        if(userDTO == null)
            return new Response<>("Provide User Data");
        User user=null;
        // Credentials are only ever set when the account is created. They
        // used to be reassigned on every save, so editing a phone number
        // reset that person's password - and would now have texted it to them.
        boolean isNew = userDTO.getUid() == null;
        if(userDTO.getUid() !=null){
            Optional<User> optionalUser = userRepository.findById(userDTO.getUid());
            if(optionalUser.isEmpty())
                return new Response<>("User Not Found");
            user=optionalUser.get();
            user.update();
        }else {
            user = new User();
        }
        user.setGender(userDTO.getGender());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setLastName(userDTO.getLastName());
        user.setMiddleName(userDTO.getMiddleName());
        user.setFirstName(userDTO.getFirstName());
        user.setDateOfBirth(userDTO.getDob());
        user.setAddress(userDTO.getAddress());
        Branch branch= null;
        if(userDTO.getBranch() !=null){
            Optional<Branch> optionalBranch = branchRepository.findById(userDTO.getBranch());
            if(optionalBranch.isEmpty())
                return new Response<>("Branch Not Found");
            branch = optionalBranch.get();
        }
        user.setBranch(branch);

        assert branch != null;

        // Held in plain only for the length of this call, to text it to them.
        // What is stored is the hash, as before.
        String plainPassword = null;
        if (isNew) {
            user.setUsername(userDTO.getFirstName() + "@" + LocalDate.now().getYear());
            plainPassword = userDTO.getLastName();
            user.setPassword(passwordEncoder.encode(plainPassword));
        }

        try{
            User saved = userRepository.save(user);

            if (isNew) {
                // After the save, and never blocking it: an account that
                // exists but whose text failed is recoverable; one that was
                // not created because a text failed is not.
                smsService.sendCredentials(
                        saved.getUid(),
                        saved.getPhone(),
                        saved.getUsername(),
                        plainPassword
                );
            }

            return new Response<>(saved);
        }catch (Exception e){
            e.printStackTrace();
            return new Response<>("Error in saving user");
        }
    }
    @Transactional
    public Response<User> deleteUser(String uid) {
        User user = userRepository.findById(uid).orElseThrow(() -> new BusinessException("User not found"));
        userRepository.deleteUserRoles(uid);
        userRepository.delete(user);
        return new Response<>(user);
    }

    public Response<UserProjection> findUserByUID(String userUID){
        log.info(LoggerUser.getEmail() + "is Accessing User");
        if(userUID == null)
            return new Response<>("Provide user ref UID");
        Optional<UserProjection> optionalUser = userRepository.findUserWithRoles(userUID);
        return optionalUser.map(Response::new).orElseGet(()->new Response<>("User Not Found"));
    }
    public ResponsePage<User> findUserPAGE(int page, int size){
        log.info(LoggerUser.getEmail() + "is Accessing User Page");
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(userRepository.findUserPage(pageable));
    }

    // Sort fields tunazokubali kutoka kwa mteja, kila moja na path yake kwenye query ya findUsers.
    private static final Map<String, String> USER_SORT_FIELDS = Map.ofEntries(
            Map.entry("username", "u.username"),
            Map.entry("firstName", "u.firstName"),
            Map.entry("middleName", "u.middleName"),
            Map.entry("lastName", "u.lastName"),
            Map.entry("email", "u.email"),
            Map.entry("phone", "u.phone"),
            Map.entry("gender", "u.gender"),
            Map.entry("dateOfBirth", "u.dateOfBirth"),
            Map.entry("address", "u.address"),
            Map.entry("isActive", "u.isActive"),
            Map.entry("isBlocked", "u.isBlocked"),
            Map.entry("isRoot", "u.isRoot"),
            Map.entry("createdAt", "u.createdAt"),
            Map.entry("updatedAt", "u.updatedAt"),
            Map.entry("branchName", "b.branchName"),
            Map.entry("branchCode", "b.branchCode")
    );

    public ResponsePage<UserProjection> findUsers(PageableParam pageableParam){
        log.info(LoggerUser.getEmail() + "is Accessing Users");
        // field isiyojulikana ingeangusha query, kwa hiyo turudi kwenye createdAt
        pageableParam.setSortBy(USER_SORT_FIELDS.getOrDefault(pageableParam.getSortBy(), "u.createdAt"));
        // Null rather than "" so the query skips the LIKE branches entirely
        // when the search box is empty.
        String search = pageableParam.getSearchParam() == null || pageableParam.getSearchParam().isBlank()
                ? null
                : pageableParam.getSearchParam().trim().toLowerCase();
        return new ResponsePage<>(userRepository.findUsers(search, pageableParam.pageable(true)));
    }
    public Response<User> assignOrUnAssignUserRole(AssignUserRoleDTO assignUserRoleDTO) {
        log.info(LoggerUser.getEmail() + " is accessing User");
        if (assignUserRoleDTO == null)
            return new Response<>("Provide Data for User");
        Optional<User> optionalUser = userRepository.findById(assignUserRoleDTO.getUserUID());
        if (optionalUser.isEmpty())
            return new Response<>("User Not Found");
        User user = optionalUser.get();
        List<Role> updatedRoles = new ArrayList<>(roleRepository.findAllById(assignUserRoleDTO.getRoleUIDS()));
        if (!seesAllRoles()) {
            // The caller (e.g. STAFF) is only shown CEO/MANAGER/CASHIER, so
            // they can neither hand out a role they can't see nor strip one
            // the user already holds - those are kept as they were.
            updatedRoles.removeIf(role -> !STAFF_VISIBLE_ROLE_CODES.contains(role.getCode()));
            if (user.getRoles() != null) {
                user.getRoles().stream()
                        .filter(role -> !STAFF_VISIBLE_ROLE_CODES.contains(role.getCode()))
                        .forEach(updatedRoles::add);
            }
        }
        user.setRoles(updatedRoles);
        return new Response<>(userRepository.save(user));
    }
    // Mirrors RoleService: ROOT and DIRECTOR manage every role, everyone
    // else only ever sees/handles the branch-operational ones.
    private static final List<String> STAFF_VISIBLE_ROLE_CODES = List.of("CEO", "MANAGER", "CASHIER");

    private boolean seesAllRoles() {
        User loggedUser = LoggerUser.getUser();
        List<String> roleCodes = loggedUser.getRoles() == null
                ? List.of()
                : loggedUser.getRoles().stream().map(Role::getCode).toList();
        return Boolean.TRUE.equals(loggedUser.getIsRoot())
                || roleCodes.contains("ROOT")
                || roleCodes.contains("DIRECTOR");
    }

    public Response<User> enableOrDisableAccount(String userUID, Boolean enable){
        log.info(LoggerUser.getEmail() + "is Enabling or Disabling User Account");
        if(userUID == null || enable == null)
            return new Response<>("Either User Ref UID or Enable Value must be provided");
        Optional<User> optionalUser = userRepository.findById(userUID);
        if(optionalUser.isEmpty())
            return new Response<>("User Not Found");
        User user = optionalUser.get();
        user.setIsBlocked(enable);
        return new Response<>(userRepository.save(user));
    }
    public Response<User> changePassword(DataDTO dataDTO) {
        log.info(LoggerUser.getEmail() + " is Changing Password");
        if (dataDTO == null) {
            return new Response<>("Provide data to change password");
        }
        String username = LoggerUser.getUser().getUsername();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new Response<>("User not found");
        }
        if (user.getIsBlocked()) {
            return new Response<>("You cannot change password, account is blocked");
        }
        if (!passwordEncoder.matches(dataDTO.getOldPassword(), user.getPassword())) {
            return new Response<>("Invalid password provided");
        }
        if (!dataDTO.getNewPassword().equals(dataDTO.getConfirmPassword())) {
            return new Response<>("Passwords do not match");
        }
        user.setPassword(passwordEncoder.encode(dataDTO.getNewPassword()));
        return new Response<>(userRepository.save(user));
    }



}
