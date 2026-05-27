package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.request.MemberRequest;
import com.LMS.LMSYS.dto.response.MemberResponse;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.exception.ConflictException;
import com.LMS.LMSYS.exception.ResourceNotFoundException;
import com.LMS.LMSYS.mapper.MemberMapper;
import com.LMS.LMSYS.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemberResponse registerMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Member with email already exists: " + request.getEmail());
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .memberSince(LocalDate.now())
                .build();

        Member saved = memberRepository.save(member);
        return MemberMapper.toResponse(saved);
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
