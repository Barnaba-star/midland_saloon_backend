package com.midland.saloon.Uaa.Controller;
import com.midland.saloon.Config.Security.JwtTokenUtil;
import com.midland.saloon.Uaa.Dto.DataDTO;
import com.midland.saloon.Uaa.Dto.LoginDTO;
import com.midland.saloon.Uaa.Model.User;
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

import java.time.LocalDateTime;
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

}
