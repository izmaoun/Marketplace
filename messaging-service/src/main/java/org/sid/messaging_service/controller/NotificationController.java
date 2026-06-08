package org.sid.messaging_service.controller;

import jakarta.validation.Valid;
import org.sid.messaging_service.dto.MissionPublishedNotification;
import org.sid.messaging_service.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/missions/published")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void missionPublished(@Valid @RequestBody MissionPublishedNotification notification) {
        notificationService.publishMissionNotification(notification);
    }
}
