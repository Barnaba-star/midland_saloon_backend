package com.midland.saloon.Uaa.Service;

import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Model.Role;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Setting.Repository.RoleRepository;
import com.midland.saloon.Uaa.Dto.*;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Projection.UserProjection;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Utils.Exceptions.BusinessException;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;


    public Response<User> saveUser(UserDTO userDTO){
        log.info(LoggerUser.getEmail() + "Is Saving User");
        if(userDTO == null)
            return new Response<>("Provide User Data");
        User user=null;
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
        user.setUsername(userDTO.getFirstName()+ "@"  + LocalDate.now().getYear());
        user.setPassword(passwordEncoder.encode(userDTO.getLastName()));
        try{
            return new Response<>(userRepository.save(user));
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

    public ResponsePage<UserProjection> findUsers(int page, int size){
        log.info(LoggerUser.getEmail() + "is Accessing Users");
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(userRepository.findUsers(pageable));
    }
    public Response<User> assignOrUnAssignUserRole(AssignUserRoleDTO assignUserRoleDTO) {
        log.info(LoggerUser.getEmail() + " is accessing User");
        if (assignUserRoleDTO == null)
            return new Response<>("Provide Data for User");
        Optional<User> optionalUser = userRepository.findById(assignUserRoleDTO.getUserUID());
        if (optionalUser.isEmpty())
            return new Response<>("User Not Found");
        User user = optionalUser.get();
        List<Role> updatedRoles = roleRepository.findAllById(assignUserRoleDTO.getRoleUIDS());
        user.setRoles(updatedRoles);
        return new Response<>(userRepository.save(user));
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
