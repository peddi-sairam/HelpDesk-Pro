package com.helpdesk.service;

import com.helpdesk.dto.AuthRequest;
import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    void register(RegisterRequest registerRequest);
    AuthResponse refreshToken(String token);
    void logout(String email);
    void initiateForgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
