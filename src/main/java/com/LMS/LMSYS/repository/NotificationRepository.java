package com.LMS.LMSYS.repository;

import com.LMS.LMSYS.entity.Notification;
import com.LMS.LMSYS.entity.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByBorrowRecordIdAndType(Long borrowRecordId, NotificationType type);

    List<Notification> findByMemberIdAndDismissedFalseOrderByCreatedAtDesc(Long memberId);
}


