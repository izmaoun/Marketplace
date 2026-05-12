package org.sid.messaging_service.service;

import java.util.List;
import java.util.Optional;
import org.sid.messaging_service.domain.Conversation;
import org.sid.messaging_service.repository.ConversationRepository;
import org.springframework.stereotype.Service;

@Service
public class ConversationService {

    private final ConversationRepository conversationRepository;

    public ConversationService(ConversationRepository conversationRepository) {
        this.conversationRepository = conversationRepository;
    }

    public Conversation createOrGet(Long missionId, Long companyId, Long freelancerId) {
        Optional<Conversation> existing = conversationRepository.findByMissionIdAndCompanyIdAndFreelancerId(
            missionId, companyId, freelancerId
        );
        if (existing.isPresent()) {
            return existing.get();
        }

        Conversation conversation = new Conversation();
        conversation.setMissionId(missionId);
        conversation.setCompanyId(companyId);
        conversation.setFreelancerId(freelancerId);
        return conversationRepository.save(conversation);
    }

    public List<Conversation> findAll() {
        return conversationRepository.findAll();
    }
}
