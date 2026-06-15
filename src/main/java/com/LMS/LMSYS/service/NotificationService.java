package com.LMS.LMSYS.service;

import com.LMS.LMSYS.dto.response.NotificationResponse;
import com.LMS.LMSYS.entity.BorrowRecord;
import com.LMS.LMSYS.entity.Notification;
import com.LMS.LMSYS.entity.NotificationType;
import com.LMS.LMSYS.exception.ResourceNotFoundException;
import com.LMS.LMSYS.mapper.NotificationMapper;
import com.LMS.LMSYS.policy.LendingPolicy;
import com.LMS.LMSYS.repository.BorrowRecordRepository;
import com.LMS.LMSYS.repository.MemberRepository;
import com.LMS.LMSYS.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    public NotificationService(
            BorrowRecordRepository borrowRecordRepository,
            MemberRepository memberRepository,
            NotificationRepository notificationRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.memberRepository = memberRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public int createDueSoonReminders(LocalDate reminderDate) {
        LocalDate earliestBorrowDate = reminderDate.minusDays(LendingPolicy.LOAN_PERIOD_DAYS);
        LocalDate latestBorrowDate = reminderDate.minusDays(
                LendingPolicy.LOAN_PERIOD_DAYS - LendingPolicy.DUE_SOON_REMINDER_DAYS);
        int createdCount = 0;

        for (BorrowRecord borrowRecord
                : borrowRecordRepository.findByBorrowDateBetweenAndReturnDateIsNull(
                        earliestBorrowDate, latestBorrowDate)) {
            if (notificationRepository.existsByBorrowRecordIdAndType(
                    borrowRecord.getId(), NotificationType.DUE_SOON)) {
                continue;
            }

            LocalDate dueDate = borrowRecord.getBorrowDate().plusDays(LendingPolicy.LOAN_PERIOD_DAYS);
            long daysUntilDue = ChronoUnit.DAYS.between(reminderDate, dueDate);
            String message = "Reminder: \"" + borrowRecord.getBook().getTitle()
                    + "\" is due on " + dueDate + ". " + getReturnReminder(daysUntilDue);

            notificationRepository.save(Notification.builder()
                    .member(borrowRecord.getMember())
                    .borrowRecord(borrowRecord)
                    .message(message)
                    .type(NotificationType.DUE_SOON)
                    .createdAt(LocalDateTime.now())
                    .read(false)
                    .dismissed(false)
                    .build());
            createdCount++;
        }

        return createdCount;
    }

    private String getReturnReminder(long daysUntilDue) {
        if (daysUntilDue == 0) {
            return "Return it today to avoid a fine.";
        }

        if (daysUntilDue == 1) {
            return "Return it within 1 day to avoid a fine.";
        }

        return "Return it within " + daysUntilDue + " days to avoid a fine.";
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMemberNotifications(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member not found with id: " + memberId);
        }

        return notificationRepository.findByMemberIdAndDismissedFalseOrderByCreatedAtDesc(memberId)
                .stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

        notification.setRead(true);
        return NotificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void dismissNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found with id: " + notificationId));

        notification.setDismissed(true);
        notificationRepository.save(notification);
    }
}
