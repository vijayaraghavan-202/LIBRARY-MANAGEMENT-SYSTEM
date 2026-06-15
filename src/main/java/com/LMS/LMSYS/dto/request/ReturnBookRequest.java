package com.LMS.LMSYS.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class ReturnBookRequest {

    @NotNull(message = "Borrow ID is required")
    private Long borrowId;

    @NotNull(message = "Member ID is required")
    private Long memberId;
}
