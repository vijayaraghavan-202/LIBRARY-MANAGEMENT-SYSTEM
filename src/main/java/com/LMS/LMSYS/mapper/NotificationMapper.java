package com.LMS.LMSYS.mapper;

import com.LMS.LMSYS.dto.response.NotificationResponse;
import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.entity.BorrowRecord;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.entity.Notification;
import com.LMS.LMSYS.policy.LendingPolicy;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        BorrowRecord borrowRecord = notification.getBorrowRecord();
        Member member = notification.getMember();
        Book book = borrowRecord != null ? borrowRecord.getBook() : null;

        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .borrowId(borrowRecord != null ? borrowRecord.getId() : null)
                .memberId(member != null ? member.getId() : null)
                .bookTitle(book != null ? book.getTitle() : null)
                .message(notification.getMessage())
                .type(notification.getType().name())
                .dueDate(borrowRecord != null
                        ? borrowRecord.getBorrowDate().plusDays(LendingPolicy.LOAN_PERIOD_DAYS)
                        : null)
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .build();
    }
}
