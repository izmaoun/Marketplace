package org.sid.messaging_service.service;

import java.time.Instant;
import java.util.List;
import org.sid.messaging_service.domain.Conversation;
import org.sid.messaging_service.domain.Message;
import org.sid.messaging_service.repository.MessageRepository;
import org.sid.messaging_service.security.MessagingUser;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(Conversation conversation, MessagingUser user, String content) {
        return saveMessage(conversation, user, content, null);
    }

    public Message saveMessage(Conversation conversation, MessagingUser user, String content, String requestedSenderRole) {
        if (!user.hasKeycloakId() || user.senderRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only conversation participants can send messages");
        }

        String senderRole = resolveSenderRole(conversation, user, requestedSenderRole);
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(resolveSenderId(conversation, user, senderRole));
        message.setSenderKeycloakId(user.keycloakId());
        message.setSenderRole(senderRole);
        message.setContent(content);
        return messageRepository.save(message);
    }

    public List<Message> getHistory(String conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    public List<Message> getHistory(String conversationId, Instant from, Instant to) {
        return getHistory(conversationId).stream()
                .filter(message -> isWithinRange(message.getSentAt(), from, to))
                .toList();
    }

    private boolean isWithinRange(Instant value, Instant from, Instant to) {
        if (value == null) {
            return from == null && to == null;
        }
        return (from == null || !value.isBefore(from)) && (to == null || !value.isAfter(to));
    }

    private Long resolveSenderId(Conversation conversation, MessagingUser user, String senderRole) {
        if ("COMPANY".equals(senderRole)) {
            return conversation.getCompanyId();
        }
        if ("FREELANCER".equals(senderRole)) {
            return conversation.getFreelancerId();
        }
        if (user.keycloakId().equals(conversation.getCompanyKeycloakId())) {
            return conversation.getCompanyId();
        }
        if (user.keycloakId().equals(conversation.getFreelancerKeycloakId())) {
            return conversation.getFreelancerId();
        }
        return user.senderId();
    }

    private String resolveSenderRole(Conversation conversation, MessagingUser user, String requestedSenderRole) {
        String normalizedRole = normalizeSenderRole(requestedSenderRole);
        if ("COMPANY".equals(normalizedRole)) {
            if (canSendAsCompany(conversation, user)) {
                return "COMPANY";
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sender role does not match conversation participant");
        }
        if ("FREELANCER".equals(normalizedRole)) {
            if (canSendAsFreelancer(conversation, user)) {
                return "FREELANCER";
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sender role does not match conversation participant");
        }
        if (requestedSenderRole != null && !requestedSenderRole.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sender role is not allowed");
        }
        if (user.keycloakId().equals(conversation.getCompanyKeycloakId())) {
            return "COMPANY";
        }
        if (user.keycloakId().equals(conversation.getFreelancerKeycloakId())) {
            return "FREELANCER";
        }
        return user.senderRole();
    }

    private boolean canSendAsCompany(Conversation conversation, MessagingUser user) {
        return user.company()
                && (user.keycloakId().equals(conversation.getCompanyKeycloakId())
                || user.keycloakId().equals(conversation.getFreelancerKeycloakId())
                || (user.companyId() != null && user.companyId().equals(conversation.getCompanyId())));
    }

    private boolean canSendAsFreelancer(Conversation conversation, MessagingUser user) {
        return user.freelancer()
                && (user.keycloakId().equals(conversation.getFreelancerKeycloakId())
                || user.keycloakId().equals(conversation.getCompanyKeycloakId())
                || (user.freelancerId() != null && user.freelancerId().equals(conversation.getFreelancerId())));
    }

    private String normalizeSenderRole(String senderRole) {
        if (senderRole == null || senderRole.isBlank()) {
            return null;
        }
        return senderRole.trim().toUpperCase();
    }
}
