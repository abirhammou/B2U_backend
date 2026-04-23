package tn.esprit.spring.b2u.service.workPost;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.repository.WorkPostRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkPostService implements IWorkPostService{

    private final WorkPostRepo workPostRepo;

    @Override
    public WorkPost create(WorkPost post) {
        return workPostRepo.save(post);
    }

    @Override
    public List<WorkPost> getAll() {
        return workPostRepo.findAll();
    }

    @Override
    public List<WorkPost> getByEntreprise(String entrepriseId) {
        return workPostRepo.findByEntrepriseId(entrepriseId);
    }

    @Override
    public WorkPost update(String id, WorkPost post) {
        post.setId(id);
        return workPostRepo.save(post);
    }

    @Override
    public void delete(String id) {
        workPostRepo.deleteById(id);
    }

    // 🔴 LOGIQUE MÉTIER
    // recommander missions selon disponibilité étudiant
    @Override
    public List<WorkPost> getRecommendedPosts(int maxHours) {
        return workPostRepo.findAll()
                .stream()
                .filter(p -> p.getHoursPerWeek() <= maxHours)
                .collect(Collectors.toList());
    }
}
