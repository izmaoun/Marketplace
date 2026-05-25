package org.sid.freelancer_service.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Freelancer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String keycloakUserId; // L'identifiant de l'utilisateur cote Keycloak

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String stripeAccountId;
    private String phone;
    private String summary;
    private String cvUrl;
    private String pfpUrl;

    @ElementCollection
    private List<String> skills;

    @ElementCollection
    private List<String> experiences;

    @ElementCollection
    private List<String> projects;

    @Column(name = "is_suspended", nullable = false)
    private boolean suspended = false;
}