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

    private String firstName;
    private String lastName;
    private String email; // Utilisé pour lier avec l'utilisateur Keycloak
    private String phone;
    private String summary;
    private String cvUrl;

    // Suppression du champ password [cite: 21]

    @ElementCollection
    private List<String> skills;

    @ElementCollection
    private List<String> experiences;

    @ElementCollection
    private List<String> projects;

    @Column(name = "is_suspended", nullable = false)
    private boolean isSuspended = false;

    // Lombok @Data génère déjà les getters/setters pour isSuspended [cite: 24, 25, 26]
}