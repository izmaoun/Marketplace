package org.sid.messaging_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.sid.messaging_service.dto.MissionPublishedNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class NotificationServiceTest {

    @Test
    void missionPublicationIsBroadcastToFreelancerTopic() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        NotificationService notificationService = new NotificationService(messagingTemplate);
        MissionPublishedNotification notification = new MissionPublishedNotification();
        notification.setMissionId(1L);
        notification.setCompanyId(10L);
        notification.setTitle("Build API");

        notificationService.publishMissionNotification(notification);

        assertThat(notification.getPublishedAt()).isNotNull();
        verify(messagingTemplate).convertAndSend(NotificationService.FREELANCER_MISSIONS_TOPIC, notification);
    }
}
