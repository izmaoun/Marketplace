package org.sid.messaging_service.service;

import java.time.Instant;
import org.sid.messaging_service.dto.MissionPublishedNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public static final String FREELANCER_MISSIONS_TOPIC = "/topic/freelancers/missions";

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishMissionNotification(MissionPublishedNotification notification) {
        if (notification.getPublishedAt() == null) {
            notification.setPublishedAt(Instant.now());
        }
        messagingTemplate.convertAndSend(FREELANCER_MISSIONS_TOPIC, notification);
    }
}
