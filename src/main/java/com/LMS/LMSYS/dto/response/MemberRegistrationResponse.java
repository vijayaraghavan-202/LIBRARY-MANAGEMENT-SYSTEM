package com.LMS.LMSYS.dto.response;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRegistrationResponse {

    private Long memberId;
    private String name;
    private String email;
    private LocalDate memberSince;
}
