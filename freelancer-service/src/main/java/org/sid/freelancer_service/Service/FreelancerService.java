package org.sid.freelancer_service.Service;

import org.sid.freelancer_service.Entity.Freelancer;
import org.sid.freelancer_service.Repository.FreelancerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FreelancerService {
    private final FreelancerRepository repository;

    public FreelancerService(FreelancerRepository repository) {
        this.repository = repository;
    }

    public List<Freelancer> getAllProfiles() {
        return repository.findAll();
    }

    public Optional<Freelancer> getProfile(Long id) {
        return repository.findById(id);
    }

    public Freelancer saveProfile(Freelancer profile) {
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
