package com.helpdesk.service.impl;

import com.helpdesk.dto.AuthRequest;
import com.helpdesk.dto.AuthResponse;
import com.helpdesk.dto.RegisterRequest;
import com.helpdesk.entity.*;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.repository.*;
import com.helpdesk.service.AuthService;
import com.helpdesk.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + authRequest.getEmail()));

        String accessToken = tokenProvider.generateToken(authentication);

        // Find or create refresh token for the user to prevent duplicate keys
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder().user(user).build());
        
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken = refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .fullName(user.getFullName())
                .userId(user.getId())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email address is already in use");
        }

        Company company = companyRepository.findById(registerRequest.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id " + registerRequest.getCompanyId()));

        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new BadRequestException("Default Customer role not initialized"));

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phone(registerRequest.getPhone())
                .role(customerRole)
                .company(company)
                .active(true)
                .build();

        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token was expired. Please sign in again.");
        }

        User user = refreshToken.getUser();
        String accessToken = tokenProvider.generateTokenFromUsername(
                user.getEmail(),
                user.getId(),
                user.getRole().getName().name(),
                user.getFullName()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().getName().name())
                .fullName(user.getFullName())
                .userId(user.getId())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .build();
    }

    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    public void initiateForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Delete existing reset tokens if any
        passwordResetTokenRepository.findByUserId(user.getId())
                .ifPresent(token -> passwordResetTokenRepository.delete(token));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Print reset link to server console for testing/verification
        String resetLink = "https://helpdesk-pro-howp.onrender.com/reset-password.html?token=" + token;
        logger.info("PASSWORD RESET LINK FOR {}: {}", email, resetLink);
        System.out.println("TESTING RESET LINK: " + resetLink);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or missing password reset token"));

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new BadRequestException("Password reset token has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }
}
