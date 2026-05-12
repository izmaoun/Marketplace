package org.sid.messaging_service.service;

import java.util.List;
import org.sid.messaging_service.domain.Conversation;
import org.sid.messaging_service.domain.Message;
import org.sid.messaging_service.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Message saveMessage(Conversation conversation, Long senderId, String senderRole, String content) {
        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setContent(content);
        return messageRepository.save(message);
    }

    public List<Message> getHistory(String conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }
}
