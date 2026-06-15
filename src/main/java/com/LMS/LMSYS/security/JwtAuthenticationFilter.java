package com.LMS.LMSYS.security;

import com.LMS.LMSYS.entity.Admin;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.repository.AdminRepository;
import com.LMS.LMSYS.repository.MemberRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String MEMBER_ROLE = "MEMBER";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            MemberRepository memberRepository,
            AdminRepository adminRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.memberRepository = memberRepository;
        this.adminRepository = adminRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws 
            ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length());

        try {
            String email = jwtService.extractEmail(token);
            String tokenId = jwtService.extractTokenId(token);
            String role = jwtService.extractRole(token);

            if (ADMIN_ROLE.equals(role)) {
                authenticateAdmin(request, token, tokenId, email);
            } else if (MEMBER_ROLE.equals(role)) {
                authenticateMember(request, token, tokenId, email);
            }
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private void authenticateMember(
            HttpServletRequest request,
            String token,
            String tokenId,
            String email) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null || tokenId == null || !tokenId.equals(member.getCurrentTokenId())) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (jwtService.isTokenValid(token, userDetails)) {
            setAuthentication(request, userDetails);
        }
    }

    private void authenticateAdmin(
            HttpServletRequest request,
            String token,
            String tokenId,
            String email) {
        Admin admin = adminRepository.findByEmail(email).orElse(null);
        if (admin == null || tokenId == null || !tokenId.equals(admin.getCurrentTokenId())) {
            return;
        }

        UserDetails userDetails = User.builder()
                .username(admin.getEmail())
                .password(admin.getPassword() == null ? "" : admin.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        if (jwtService.isTokenValid(token, userDetails)) {
            setAuthentication(request, userDetails);
        }
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
