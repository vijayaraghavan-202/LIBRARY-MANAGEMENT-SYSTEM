package com.LMS.LMSYS.controller;

import com.LMS.LMSYS.dto.response.NotificationResponse;
import com.LMS.LMSYS.service.NotificationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/members/{memberId}/notifications")
    public ResponseEntity<List<NotificationResponse>> getMemberNotifications(@PathVariable Long memberId) {
        return ResponseEntity.ok(notificationService.getMemberNotifications(memberId));
    }

    @PatchMapping("/notifications/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }

    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<Void> dismissNotification(@PathVariable Long notificationId) {
        notificationService.dismissNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
