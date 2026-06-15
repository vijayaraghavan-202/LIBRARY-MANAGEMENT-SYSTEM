package com.LMS.LMSYS.mapper;

import com.LMS.LMSYS.dto.response.MemberLoginResponse;
import com.LMS.LMSYS.dto.response.MemberRegistrationResponse;
import com.LMS.LMSYS.dto.response.MemberResponse;
import com.LMS.LMSYS.entity.Member;

public final class MemberMapper {

    private MemberMapper() {
    }

    public static MemberResponse toResponse(Member member) {
        if (member == null) {
            return null;
        }

        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .memberSince(member.getMemberSince())
                .build();
    }

    public static MemberRegistrationResponse toRegistrationResponse(Member member) {
        if (member == null) {
            return null;
        }

        return MemberRegistrationResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .memberSince(member.getMemberSince())
                .build();
    }

    public static MemberLoginResponse toLoginResponse(Member member) {
        return toLoginResponse(member, null, null);
    }

    public static MemberLoginResponse toLoginResponse(Member member, String token, Long expiresIn) {
        if (member == null) {
            return null;
        }

        return MemberLoginResponse.builder()
                .role("MEMBER")
                .memberId(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .memberSince(member.getMemberSince())
                .token(token)
                .expiresIn(expiresIn)
                .build();
    }
}
