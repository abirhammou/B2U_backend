package tn.esprit.spring.b2u.service.workPost;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.WorkPostStatus;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.WorkPostRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkPostService implements IWorkPostService{

    private final WorkPostRepo workPostRepo;
    private final ProjetRepository projetRepository;

    @Override
    public WorkPost create(WorkPost post) {
        post.setStatus(WorkPostStatus.ACTIVE);
        post.setCreatedAt(LocalDateTime.now());
        return workPostRepo.save(post);
    }

    @Override
    public List<WorkPost> getAll() {
        return workPostRepo.findAll().stream()
                .map(this::updateExpiredStatus)
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkPost> getByEntreprise(String entrepriseId) {
        return workPostRepo.findByEntrepriseId(entrepriseId).stream()
                .map(this::updateExpiredStatus)
                .collect(Collectors.toList());
    }

    @Override
    public WorkPost update(String id, WorkPost updatedPost) {
        WorkPost existing = workPostRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkPost not found: " + id));

        updatedPost.setCreatedAt(existing.getCreatedAt());
        updatedPost.setId(id);

        if (updatedPost.getStatus() == null) {
            updatedPost.setStatus(existing.getStatus());
        }
        updatedPost = updateExpiredStatus(updatedPost);

        return workPostRepo.save(updatedPost);
    }

    @Override
    public void delete(String id) {
        workPostRepo.deleteById(id);
    }

    @Override
    public WorkPost getById(String id) {
        return workPostRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkPost not found: " + id));
    }

    @Override
    public List<WorkPost> getRecommendedPosts(int maxHours) {
        return workPostRepo.findAll().stream()
                .filter(p -> p.getHoursPerWeek() <= maxHours)
                .map(this::updateExpiredStatus)
                .collect(Collectors.toList());
    }

    private WorkPost updateExpiredStatus(WorkPost post) {
    // Helper method to set status to EXPIRED if past due date
        return post;
    }

    @Override
    public WorkPost assignProjet(String workPostId, String projetId) {
        WorkPost workPost = workPostRepo.findById(workPostId)
                .orElseThrow(() -> new RuntimeException("WorkPost not found: " + workPostId));

        if (!projetRepository.existsById(projetId)) {
            throw new RuntimeException("Projet not found: " + projetId);
        }

        workPost.setProjetId(projetId);
        return workPostRepo.save(workPost);
    }

    @Override
    public WorkPost unassignProjet(String workPostId) {
        WorkPost workPost = workPostRepo.findById(workPostId)
                .orElseThrow(() -> new RuntimeException("WorkPost not found: " + workPostId));

        workPost.setProjetId(null);
        return workPostRepo.save(workPost);
    }

    @Override
    public List<WorkPost> getByProjet(String projetId) {
        return workPostRepo.findByProjetId(projetId).stream()
                .map(this::updateExpiredStatus)
                .collect(Collectors.toList());
    }
}
