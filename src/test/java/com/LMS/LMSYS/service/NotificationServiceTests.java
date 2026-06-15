package com.LMS.LMSYS.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.LMS.LMSYS.entity.Book;
import com.LMS.LMSYS.entity.BorrowRecord;
import com.LMS.LMSYS.entity.Member;
import com.LMS.LMSYS.entity.Notification;
import com.LMS.LMSYS.entity.NotificationType;
import com.LMS.LMSYS.repository.BorrowRecordRepository;
import com.LMS.LMSYS.repository.MemberRepository;
import com.LMS.LMSYS.repository.NotificationRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                borrowRecordRepository,
                memberRepository,
                notificationRepository);
    }

    @Test
    void createsReminderForActiveLoanTwoDaysBeforeDueDate() {
        LocalDate reminderDate = LocalDate.of(2026, 6, 14);
        BorrowRecord borrowRecord = createBorrowRecord(LocalDate.of(2026, 6, 2));
        when(borrowRecordRepository.findByBorrowDateBetweenAndReturnDateIsNull(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 2)))
                .thenReturn(List.of(borrowRecord));
        when(notificationRepository.existsByBorrowRecordIdAndType(10L, NotificationType.DUE_SOON))
                .thenReturn(false);

        int createdCount = notificationService.createDueSoonReminders(reminderDate);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();

        assertEquals(1, createdCount);
        assertEquals(10L, notification.getBorrowRecord().getId());
        assertEquals(2L, notification.getMember().getId());
        assertEquals(NotificationType.DUE_SOON, notification.getType());
        assertTrue(notification.getMessage().contains("2026-06-16"));
        assertTrue(notification.getMessage().contains("within 2 days"));
        assertFalse(notification.isRead());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void createsDynamicReminderForLoanDueTomorrow() {
        LocalDate reminderDate = LocalDate.of(2026, 6, 14);
        BorrowRecord borrowRecord = createBorrowRecord(LocalDate.of(2026, 6, 1));
        when(borrowRecordRepository.findByBorrowDateBetweenAndReturnDateIsNull(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 2)))
                .thenReturn(List.of(borrowRecord));
        when(notificationRepository.existsByBorrowRecordIdAndType(10L, NotificationType.DUE_SOON))
                .thenReturn(false);

        notificationService.createDueSoonReminders(reminderDate);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("within 1 day"));
    }

    @Test
    void createsDynamicReminderForLoanDueToday() {
        LocalDate reminderDate = LocalDate.of(2026, 6, 14);
        BorrowRecord borrowRecord = createBorrowRecord(LocalDate.of(2026, 5, 31));
        when(borrowRecordRepository.findByBorrowDateBetweenAndReturnDateIsNull(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 2)))
                .thenReturn(List.of(borrowRecord));
        when(notificationRepository.existsByBorrowRecordIdAndType(10L, NotificationType.DUE_SOON))
                .thenReturn(false);

        notificationService.createDueSoonReminders(reminderDate);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(notificationCaptor.capture());
        assertTrue(notificationCaptor.getValue().getMessage().contains("Return it today"));
    }

    @Test
    void skipsDuplicateReminderForSameLoan() {
        LocalDate reminderDate = LocalDate.of(2026, 6, 14);
        BorrowRecord borrowRecord = createBorrowRecord(LocalDate.of(2026, 6, 2));
        when(borrowRecordRepository.findByBorrowDateBetweenAndReturnDateIsNull(
                LocalDate.of(2026, 5, 31), LocalDate.of(2026, 6, 2)))
                .thenReturn(List.of(borrowRecord));
        when(notificationRepository.existsByBorrowRecordIdAndType(10L, NotificationType.DUE_SOON))
                .thenReturn(true);

        int createdCount = notificationService.createDueSoonReminders(reminderDate);

        assertEquals(0, createdCount);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void dismissesNotificationWithoutDeletingReminderHistory() {
        Notification notification = Notification.builder()
                .id(5L)
                .dismissed(false)
                .build();
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        notificationService.dismissNotification(5L);

        assertTrue(notification.isDismissed());
        verify(notificationRepository).save(notification);
        verify(notificationRepository, never()).delete(any(Notification.class));
    }

    private BorrowRecord createBorrowRecord(LocalDate borrowDate) {
        Book book = Book.builder()
                .id(1L)
                .title("Clean Code")
                .build();
        Member member = Member.builder()
                .id(2L)
                .name("Vijay")
                .build();

        return BorrowRecord.builder()
                .id(10L)
                .book(book)
                .member(member)
                .borrowDate(borrowDate)
                .build();
    }
}
