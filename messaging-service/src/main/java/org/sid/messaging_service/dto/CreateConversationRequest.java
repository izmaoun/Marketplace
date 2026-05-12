package org.sid.messaging_service.dto;

import jakarta.validation.constraints.NotNull;

public class CreateConversationRequest {

    @NotNull
    private Long missionId;

    @NotNull
    private Long companyId;

    @NotNull
    private Long freelancerId;

    public Long getMissionId() {
        return missionId;
    }

    public void setMissionId(Long missionId) {
        this.missionId = missionId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getFreelancerId() {
        return freelancerId;
    }

    public void setFreelancerId(Long freelancerId) {
        this.freelancerId = freelancerId;
    }
}

