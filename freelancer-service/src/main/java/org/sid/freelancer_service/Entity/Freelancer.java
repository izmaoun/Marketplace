package org.sid.freelancer_service.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class Freelancer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String summary;
    private String password;
    private String cvUrl;

    @ElementCollection
    private List<String> skills;

    @ElementCollection
    private List<String> experiences;

    @ElementCollection
    private List<String> projects;

    @Column(name = "is_suspended", nullable = false)
    private boolean isSuspended = false;

    public boolean isSuspended() {
        return isSuspended;
    }

    public void setSuspended(boolean suspended) {
        isSuspended = suspended;
    }
}
