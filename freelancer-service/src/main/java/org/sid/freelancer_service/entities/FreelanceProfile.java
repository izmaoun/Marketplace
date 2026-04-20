package org.sid.freelancer_service.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class FreelanceProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String summary;
    private String cvUrl;

    @ElementCollection
    private List<String> skills;

    @ElementCollection
    private List<String> experiences;

    @ElementCollection
    private List<String> projects;
}
