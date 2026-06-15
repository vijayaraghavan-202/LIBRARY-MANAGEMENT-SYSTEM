package com.LMS.LMSYS.service;

import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DueSoonNotificationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DueSoonNotificationScheduler.class);
    private static final ZoneId SCHEDULER_ZONE = ZoneId.of("Asia/Kolkata");

    private final NotificationService notificationService;

    public DueSoonNotificationScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = " 0 0 */9 * * * ", zone = "Asia/Kolkata")
    public void createDueSoonReminders() {
        int createdCount = notificationService.createDueSoonReminders(LocalDate.now(SCHEDULER_ZONE));
        LOGGER.info("Created {} due-soon notification(s)", createdCount);
    }
}
