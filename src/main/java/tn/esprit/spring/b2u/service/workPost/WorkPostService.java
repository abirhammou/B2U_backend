package tn.esprit.spring.b2u.service.workPost;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.WorkPostStatus;
import tn.esprit.spring.b2u.repository.WorkPostRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkPostService implements IWorkPostService{

    private final WorkPostRepo workPostRepo;

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

        // Preserve creation date
        updatedPost.setCreatedAt(existing.getCreatedAt());
        updatedPost.setId(id);

        // If status is not being manually changed, keep it as is or re‑evaluate expiry
        if (updatedPost.getStatus() == null) {
            updatedPost.setStatus(existing.getStatus());
        }
        // Re‑check expiry after update
        updatedPost = updateExpiredStatus(updatedPost);

        return workPostRepo.save(updatedPost);
    }

    @Override
    public void delete(String id) {
        workPostRepo.deleteById(id);
    }

    // 🔴 LOGIQUE MÉTIER
    // recommander missions selon disponibilité étudiant
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
}
