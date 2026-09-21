package com.midland.saloon.Setting.Service;
import com.midland.saloon.Config.Security.LoggerUser;
import com.midland.saloon.Setting.Dto.BranchDTO;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Model.BranchCodeHelper;
import com.midland.saloon.Setting.Projection.BranchProjection;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Utils.Responses.Response;
import com.midland.saloon.Utils.Responses.ResponseList;
import com.midland.saloon.Utils.Responses.ResponsePage;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log
public class BranchService {
    private final BranchRepository branchRepository;
    private final BranchCodeHelper branchCodeHelper;
    private final UserRepository userRepository;
    public Response<Branch> saveBranch(BranchDTO branchDTO) {

        log.info(LoggerUser.getEmail() + " is saving Branch");

        if (branchDTO == null) {
            return new Response<>("Provide Branch Details");
        }

        if (branchDTO.getBranchName() == null ||
                branchDTO.getBranchName().isBlank()) {
            return new Response<>("Provide Branch Name");
        }

        if (branchDTO.getRegion() == null ||
                branchDTO.getRegion().isBlank()) {
            return new Response<>("Provide Region");
        }

        try {

            Branch branch;

            /*
             * ==========================
             * UPDATE
             * ==========================
             */
            if (branchDTO.getUid() != null) {

                Optional<Branch> optionalBranch =
                        branchRepository.findById(branchDTO.getUid());

                if (optionalBranch.isEmpty()) {
                    return new Response<>("Branch Not Found");
                }

                branch = optionalBranch.get();

            }

            /*
             * ==========================
             * CREATE
             * ==========================
             */
            else {

                branch = new Branch();

                Integer lastSequence = branchRepository.findLastBranchSequenceByRegion(branchDTO.getRegion());
                long nextNumber = (lastSequence == null ? 0 : lastSequence) + 1;
                String branchCode = branchCodeHelper.generateBranchCode(branchDTO.getRegion(), nextNumber);
                branch.setBranchCode(branchCode);
            }


            /*
             * ==========================
             * SET BRANCH DETAILS
             * ==========================
             */

            branch.setBranchName(
                    branchDTO.getBranchName()
            );

            branch.setBranchCategory(
                    branchDTO.getBranchCategory()
            );



            branch.setDescription(
                    branchDTO.getDescription()
            );

            branch.setAddress(
                    branchDTO.getAddress()
            );

            branch.setPhone(
                    branchDTO.getPhone()
            );

            branch.setRegion(
                    branchDTO.getRegion()
            );

            branch.setStatus(
                    branchDTO.getStatus()
            );


            /*
             * ==========================
             * SAVE
             * ==========================
             */

            Branch savedBranch =
                    branchRepository.save(branch);

            return new Response<>(savedBranch);

        } catch (NumberFormatException e) {

            e.printStackTrace();
            return new Response<>(
                    "Invalid Branch Code Format"
            );

        } catch (Exception e) {


            return new Response<>(
                    "Error in saving Branch"
            );
        }
    }




    public Response<Branch> findBranchByUID(String branchUID){
        log.info(LoggerUser.getEmail() + " is Accessing Branch");
        if(branchUID == null)
            return new Response<>("Branch UID is required");
        Optional<Branch> optionalBranch = branchRepository.findById(branchUID);
        return optionalBranch.map(Response::new).orElseGet(() -> new Response<>("Branch Not Found"));
    }
    public ResponsePage<Branch> findBranchPage(int page, int size){
        log.info(LoggerUser.getEmail() + "is accessing Branch");
        Pageable pageable = PageRequest.of(page, size);
        return new ResponsePage<>(branchRepository.findAll(pageable));
    }
    public Response<Branch> deleteBranch(String branchUID){
        log.info(LoggerUser.getEmail() + "is deleting Branch");
        Optional<Branch> optionalBranch = branchRepository.findById(branchUID);
        if(optionalBranch.isEmpty())
            return new Response<>("Branch Not Found");
        branchRepository.delete(optionalBranch.get());
        return new Response<>(optionalBranch.get());
    }
    public ResponseList<BranchProjection> findBranchList(){
        log.info(LoggerUser.getEmail() + "is accessing Branch");
        return new ResponseList<>(branchRepository.findBranchList());
    }

    public ResponseList<User> findAllUsersWithBranchAndRoles(String branchUID){
        log.info(LoggerUser.getEmail() + "is accessing User and Branch");
        if(branchUID==null)
            return new ResponseList<>("Provide Branch REF");
        return new ResponseList<>(userRepository.findAllUsersWithBranchAndRoles(branchUID));
    }
}
