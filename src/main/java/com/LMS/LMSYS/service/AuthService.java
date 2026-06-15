package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.LoginRequest;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.exception.UnauthorizedException;
import com.LMS.LMSYS.mapper.MemberMapper;
import com.LMS.LMSYS.repository.AdminRepository;
import com.LMS.LMSYS.repository.MemberRepository;
import com.LMS.LMSYS.security.CustomUserDetailsService;
import com.LMS.LMSYS.security.JwtService;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid email or password";

    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final AdminAuthService adminAuthService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthService(
            MemberRepository memberRepository,
            AdminRepository adminRepository,
            AdminAuthService adminAuthService,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService,
            JwtService jwtService) {
        this.memberRepository = memberRepository;
        this.adminRepository = adminRepository;
        this.adminAuthService = adminAuthService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Transactional
    public Object login(LoginRequest request) {
        if (adminRepository.findByEmail(request.getEmail()).isPresent()) {
            return adminAuthService.login(request);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }

        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE));

        String tokenId = UUID.randomUUID().toString();
        member.setCurrentTokenId(tokenId);
        Member saved = memberRepository.save(member);

        UserDetails userDetails = userDetailsService.loadUserByUsername(saved.getEmail());
        String token = jwtService.generateToken(userDetails, tokenId);

        return MemberMapper.toLoginResponse(saved, token, jwtService.getExpirationMs());
    }

    

    

    

    @Transactional
    public void logout(String email) {
        adminRepository.findByEmail(email).ifPresent(admin -> {
            admin.setCurrentTokenId(null);
            adminRepository.save(admin);
        });

        memberRepository.findByEmail(email).ifPresent(member -> {
            member.setCurrentTokenId(null);
            memberRepository.save(member);
        });
    }
}
