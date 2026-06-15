package com.LMS.LMSYS.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
public class NotificationResponse {

    private Long notificationId;
    private Long borrowId;
    private Long memberId;
    private String bookTitle;
    private String message;
    private String type;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private boolean read;
}
