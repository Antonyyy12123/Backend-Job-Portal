package com.ey.controller;
 
import com.ey.dto.*;
import com.ey.service.AuthService;
import com.ey.exception.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
 
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
 
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getRole() != null && "ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new ForbiddenException("Cannot create admin accounts");
        }
        return ResponseEntity.status(201).body(authService.register(request));
    }
 
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
 
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }
 
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }
 
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto dto, Authentication auth) {
        String username = auth.getName();
        authService.changePassword(username, dto.getOldPassword(), dto.getNewPassword());
        SimpleResponse resp = new SimpleResponse();
        resp.setMessage("Password changed");
        return ResponseEntity.ok(resp);
    }
}