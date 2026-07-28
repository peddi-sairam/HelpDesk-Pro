package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.dto.AuthRequest;
import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.login(authRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok(new ApiResponse<>(true, "Registration successful. Please login."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@RequestParam String token) {
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestParam String email) {
        authService.logout(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        authService.initiateForgotPassword(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset link sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestParam String token, @RequestParam String password) {
        authService.resetPassword(token, password);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successful."));
    }
}
