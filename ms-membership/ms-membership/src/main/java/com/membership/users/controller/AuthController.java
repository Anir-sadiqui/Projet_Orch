package com.membership.users.controller;



import com.membership.users.security.JwtService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {


        if ("user@example.com".equals(request.getEmail()) &&
                "password123".equals(request.getPassword())) {


            String token = jwtService.generateToken(
                    1L,
                    request.getEmail(),
                    List.of("USER")
            );

            return ResponseEntity.ok(new LoginResponse(token, 3600));
        }


        return ResponseEntity.status(401).build();
    }


    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResponse {
        private String token;
        private long expiresIn;
    }
}

