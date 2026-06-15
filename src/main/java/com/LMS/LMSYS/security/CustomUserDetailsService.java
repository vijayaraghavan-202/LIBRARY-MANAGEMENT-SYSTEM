package com.LMS.LMSYS.security;

import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.repository.MemberRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomUserDetailsService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found with email: " + email));

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword() == null ? "" : member.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_MEMBER")))
                .build();
    }
}
