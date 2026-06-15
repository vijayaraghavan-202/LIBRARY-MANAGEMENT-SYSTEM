package com.LMS.LMSYS.dto.response;

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
public class AdminLoginResponse {

    private String role;
    private Long adminId;
    private String name;
    private String email;
    private String token;
    private Long expiresIn;
}
