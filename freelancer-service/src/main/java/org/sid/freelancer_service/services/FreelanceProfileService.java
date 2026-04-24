package org.sid.freelancer_service.services;

import org.sid.freelancer_service.entities.FreelanceProfile;
import org.sid.freelancer_service.repositories.FreelanceProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FreelanceProfileService {
    private final FreelanceProfileRepository repository;

    public FreelanceProfileService(FreelanceProfileRepository repository) {
        this.repository = repository;
    }

    public List<FreelanceProfile> getAllProfiles() {
        return repository.findAll();
    }

    public Optional<FreelanceProfile> getProfile(Long id) {
        return repository.findById(id);
    }

    public FreelanceProfile saveProfile(FreelanceProfile profile) {
        return repository.save(profile);
    }

    public void deleteProfile(Long id) {
        repository.deleteById(id);
    }

    public void suspendProfile(Long id) {
        repository.findById(id).ifPresent(profile -> {
            profile.setSuspended(true);
            repository.save(profile);
        });
    }
}
