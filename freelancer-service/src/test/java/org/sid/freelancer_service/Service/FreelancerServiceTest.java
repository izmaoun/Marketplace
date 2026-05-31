package org.sid.freelancer_service.Service;

import jakarta.persistence.Column;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sid.freelancer_service.DTO.FreelancerRequest;
import org.sid.freelancer_service.DTO.FreelancerResponse;
import org.sid.freelancer_service.DTO.StripeAccountOnboardingResponse;
import org.sid.freelancer_service.Entity.Freelancer;
import org.sid.freelancer_service.Repository.FreelancerRepository;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreelancerServiceTest {

    @Mock
    private FreelancerRepository repository;
    @Mock
    private MissionServiceClient missionServiceClient;
    @Mock
    private RestTemplate restTemplate;

    @Test
    void createFreelancerWhenPaymentDisabledSkipsStripeCreation() {
        when(repository.existsByEmail("abdo1.test@example.com")).thenReturn(false);
        when(repository.existsByKeycloakUserId("6916376e-2e74-4231-a5f8-e46e603d560f")).thenReturn(false);
        when(repository.save(any(Freelancer.class))).thenAnswer(invocation -> {
            Freelancer freelancer = invocation.getArgument(0);
            if (freelancer.getId() == null) {
                freelancer.setId(1L);
            }
            return freelancer;
        });

        FreelancerService service = new FreelancerService(
                repository,
                missionServiceClient,
                restTemplate,
                "http://payment-service:8088",
                false
        );

        FreelancerRequest request = new FreelancerRequest(
                "6916376e-2e74-4231-a5f8-e46e603d560f",
                "abdo1.test@example.com",
                "Abdo",
                "Test",
                "+212611111111",
                "Developpeur Java Spring Boot",
                "https://example.com/cv.pdf",
                "https://example.com/profile.png"
        );

        FreelancerResponse response = service.createFreelancer(request);

        assertEquals(1L, response.getId());
        assertEquals(request.getKeycloakUserId(), response.getKeycloakUserId());
        assertNull(response.getStripeAccountId());
        assertNull(response.getStripeOnboardingUrl());

        verify(restTemplate, never()).postForObject(anyString(), any(), eq(StripeAccountOnboardingResponse.class));
        verify(repository, times(1)).save(any(Freelancer.class));
    }

    @Test
    void keycloakUserIdFieldMapsToKeycloakIdColumn() throws NoSuchFieldException {
        Field keycloakField = Freelancer.class.getDeclaredField("keycloakUserId");
        Column column = keycloakField.getAnnotation(Column.class);

        assertNotNull(column);
        assertEquals("keycloak_id", column.name());
        assertFalse(column.nullable());
    }
}
