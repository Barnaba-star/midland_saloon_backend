package com.midland.saloon.Uaa.Controller;
import com.midland.saloon.Config.Security.JwtTokenUtil;
import com.midland.saloon.Setting.Model.Branch;
import com.midland.saloon.Uaa.Dto.DataDTO;
import com.midland.saloon.Uaa.Dto.LoginDTO;
import com.midland.saloon.Uaa.Model.User;
import jakarta.validation.Valid;
import com.midland.saloon.Setting.Dto.ExpiredSubscriptionPaymentDTO;
import com.midland.saloon.Setting.Service.SettingService;
import com.midland.saloon.Setting.Service.PlatformSettingService;
import com.midland.saloon.Uaa.Repository.UserRepository;
import com.midland.saloon.Uaa.Service.UserService;
import com.midland.saloon.Utils.Responses.Response;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/authentication")
@RequiredArgsConstructor
public class UserController {
    @Autowired
    private  UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final PlatformSettingService platformSettingService;
    private final SettingService settingService;




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        if (loginDTO == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Provide Login Details");
        }

        User user = userRepository.findByUsername(loginDTO.getUsername());
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("User Not Found");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Username or Password");
        }

        if (Boolean.TRUE.equals(user.getIsBlocked())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Account Blocked");
        }

        // Branch must be within its free/paid period to log in. ROOT users
        // and the platform's own ROOT branch are exempt - they're not a
        // paying customer branch. closeSubscription == null means an admin
        // hasn't configured a plan for this branch yet, which we treat as
        // unrestricted rather than locking brand new branches out before
        // anyone's had a chance to set one up.
        Branch branch = user.getBranch();
        boolean exempt = Boolean.TRUE.equals(user.getIsRoot())
                || (branch != null && "ROOT".equalsIgnoreCase(branch.getBranchCode()));

        // A grace period keeps a branch working for a few days past its end
        // date rather than locking them out the same morning. Zero - the
        // default - is the old behaviour exactly.
        Integer graceDays = platformSettingService.current().getGracePeriodDays();
        LocalDate lockoutDate = LocalDate.now().minusDays(graceDays == null ? 0 : graceDays);

        if (!exempt
                && branch != null
                && branch.getCloseSubscription() != null
                && branch.getCloseSubscription().isBefore(lockoutDate)) {
            // Structured rather than plain text: the login screen needs to
            // tell this apart from a wrong password so it can offer a way to
            // pay, and it needs the monthly figure to price the months.
            Map<String, Object> expired = new LinkedHashMap<>();
            expired.put("status", 403);
            expired.put("code", "SUBSCRIPTION_EXPIRED");
            expired.put("message", "Subscription Expired. Please pay to continue using the system.");
            expired.put("branchName", branch.getBranchName());
            expired.put("monthlyAmount", branch.getSubscriptionAmount());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(expired);
        }

        try {
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            String token = jwtTokenUtil.generateToken(user);
            Cookie cookie = new Cookie("jwt_token", token);
            cookie.setHttpOnly(false);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24);
            cookie.setSecure(false);
            response.addCookie(cookie);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error in setting Token");
        }
    }

    @PostMapping("/changePassword")
    public Response<User> changePassword(@RequestBody DataDTO dataDTO){
        return userService.changePassword(dataDTO);
    }

    /**
     * The way out of the dead end: a branch whose subscription has lapsed
     * cannot log in, so it cannot reach the normal payment screen either.
     * This sits under /authentication (open by design) and re-checks the
     * credentials itself. It issues no token - paying is all it does.
     */
    @PostMapping("/paySubscription")
    public Response<Branch> paySubscription(@Valid @RequestBody ExpiredSubscriptionPaymentDTO dto) {
        return settingService.payExpiredSubscription(dto);
    }
}
