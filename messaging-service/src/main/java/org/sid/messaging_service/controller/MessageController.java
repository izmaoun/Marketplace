package org.sid.messaging_service.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.sid.messaging_service.domain.Conversation;
import org.sid.messaging_service.domain.Message;
import org.sid.messaging_service.dto.MessageRequest;
import org.sid.messaging_service.dto.MessageResponse;
import org.sid.messaging_service.repository.ConversationRepository;
import org.sid.messaging_service.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final ConversationRepository conversationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(
            MessageService messageService,
            ConversationRepository conversationRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messageService = messageService;
        this.conversationRepository = conversationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/conversation/{conversationId}")
    public List<MessageResponse> history(@PathVariable String conversationId) {
        return messageService.getHistory(conversationId).stream().map(this::toResponse).toList();
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload MessageRequest request, Principal principal) {
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        Long senderId = request.getSenderId() != null ? request.getSenderId() : extractUserId(principal);
        String senderRole = request.getSenderRole() != null ? request.getSenderRole() : "UNKNOWN";

        Message saved = messageService.saveMessage(conversation, senderId, senderRole, request.getContent());

        MessageResponse response = toResponse(saved);
        messagingTemplate.convertAndSend(
                "/topic/conversations/" + conversation.getId(),
                response
        );
    }

    private Long extractUserId(Principal principal) {
        if (principal != null) {
            try {
                return Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getSenderId(),
                message.getSenderRole(),
                message.getContent(),
                message.getSentAt()
        );
    }
}