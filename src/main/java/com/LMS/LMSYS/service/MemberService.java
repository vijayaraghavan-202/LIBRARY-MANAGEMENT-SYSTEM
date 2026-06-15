package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.MemberRequest;
import com.LMS.LMSYS.dto.response.MemberRegistrationResponse;
import com.LMS.LMSYS.dto.response.MemberResponse;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.exception.ConflictException;
import com.LMS.LMSYS.exception.ResourceNotFoundException;
import com.LMS.LMSYS.mapper.MemberMapper;
import com.LMS.LMSYS.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public MemberRegistrationResponse registerMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Member already exists with this email");
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .memberSince(LocalDate.now())
                .build();

        Member saved = memberRepository.save(member);
        return MemberMapper.toRegistrationResponse(saved);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
        return MemberMapper.toResponse(member);
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberMapper::toResponse)
                .toList();
    }
}
