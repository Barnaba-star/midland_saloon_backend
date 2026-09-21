package com.midland.saloon.Config.Initilizer;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Setting.Model.Role;
import com.midland.saloon.Setting.Repository.BranchRepository;
import com.midland.saloon.Setting.Repository.RoleRepository;
import com.midland.saloon.Uaa.Model.Permission;
import com.midland.saloon.Uaa.Model.User;
import com.midland.saloon.Uaa.Repository.PermissionRepository;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.univocity.parsers.common.record.Record;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class Initializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Initializer.class);
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BranchRepository branchRepository;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedPermissions();
        seedSuperRole();
        seedSuperUser();
        seedRootBranch();
    }

    public void seedPermissions() throws Exception {
        log.info("************************Seed Permissions******************************");
        InputStream inputStream = null;
        try{
            inputStream = new ClassPathResource("/Seed/Permission.csv").getInputStream();

        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        CsvParserSettings csvParserSettings = new CsvParserSettings();
        csvParserSettings.setHeaderExtractionEnabled(true);
        CsvParser parser = new CsvParser(csvParserSettings);
        List<Record> recordList = parser.parseAllRecords(inputStream);
        for(Record record:recordList){
            if(record.getString("name") != null){
                Optional<Permission> permission = permissionRepository.findByName(record.getString("name"));
                if(permission.isEmpty()){
                   Permission permission1 = new Permission();
                   permission1.setName(record.getString("name"));
                   permission1.setGroup(record.getString("group"));
                   permission1.setModule(record.getString("module"));
                   try {
                       permissionRepository.save(permission1);
                   }catch (Exception e){
                       e.printStackTrace();
                       throw new Exception("Error Occurred");
                   }
                }
            }
        }
    }



    public void seedSuperRole() throws Exception{
        log.info("***************************Seed Supper Role*****************************");
        Role roleName = roleRepository.findFirstByName("ROOT");
        if(roleName == null){
            Role role = new Role();
            role.setCode("ROOT");
            role.setName("ROOT");
            role.setDescription("SUPER ROLE");
            role.setPermission(Collections.emptyList());
            try{
                roleRepository.save(role);
            }catch (Exception e){
                e.printStackTrace();
                throw new Exception("Error in Seeding Super Role");
            }
        }
    }

    public void seedRootBranch() throws Exception{
        log.info("*****************************Seed Main Branch***************************");
        Optional<Branch> optionalBranch = branchRepository.findRootBranch("ROOT BRANCH");
        if(optionalBranch.isEmpty()){
            Branch branch = new Branch();
            branch.setBranchName("ROOT BRANCH");
            branch.setBranchCategory("ALL CATEGORIES");
            branch.setBranchCode("ROOT");
            branch.setRegion("DODOMA");
            branch.setStatus("ACTIVE");
            branch.setAddress("DODOMA");
            branch.setPhone("0000-0000-0000");
            branch.setDescription("BRANCH FOR SUPER USER ACTIVITIES FOR ALL BRANCHES");
            try{
                branchRepository.save(branch);
            }catch (Exception e){
                e.printStackTrace();
                throw new Exception("Error in seeding Root Branch");
            }
        }
    }

    public void seedSuperUser() throws Exception{
        log.info("*****************************Seed Super User***************************");
        User user = userRepository.findFirstByUsername("root@root.com");
        Role role = roleRepository.findByCode("ROOT");
        Branch branch = branchRepository.findByBranchCode("ROOT");
        if(user == null){
            User superUser = new User();
            superUser.setUsername("root@root.com");
            superUser.setPassword(passwordEncoder.encode("root"));
            superUser.setIsRoot(true);
            superUser.setEmail("root@root.com");
            superUser.setGender("MALE");
            superUser.setAddress("DODOMA");
            superUser.setFirstName("ROOT");
            superUser.setMiddleName("ROOT");
            superUser.setLastName("ROOT");
            superUser.setDateOfBirth(LocalDate.now());
            superUser.setPhone("1010101010");
            superUser.setRoles(List.of(role));
            superUser.setBranch(branch);
            try {
                userRepository.save(superUser);
            }catch (Exception e){
                e.printStackTrace();
                throw new Exception("Error in Saving Super User");
            }
        }
    }




}
