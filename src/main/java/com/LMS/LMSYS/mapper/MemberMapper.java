package com.LMS.LMSYS.mapper;

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
}
