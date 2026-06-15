package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.LoginRequest;
import com.LMS.LMSYS.dto.response.AdminLoginResponse;
import com.LMS.LMSYS.entity.Admin;
import com.LMS.LMSYS.exception.UnauthorizedException;
import com.LMS.LMSYS.repository.AdminRepository;
import com.LMS.LMSYS.security.JwtService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AdminLoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE));

        if (admin.getPassword() == null
                || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }

        String tokenId = UUID.randomUUID().toString();
        admin.setCurrentTokenId(tokenId);
        Admin saved = adminRepository.save(admin);

        UserDetails userDetails = User.builder()
                .username(saved.getEmail())
                .password(saved.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        String token = jwtService.generateToken(userDetails, tokenId);

        return AdminLoginResponse.builder()
                .role("ADMIN")
                .adminId(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .token(token)
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }
}
