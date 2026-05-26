package tn.esprit.spring.b2u.service.workPost;

import tn.esprit.spring.b2u.entity.WorkPost;

import java.util.List;

public interface IWorkPostService {

    WorkPost create(WorkPost post);
    List<WorkPost> getAll();
    List<WorkPost> getByEntreprise(String entrepriseId);
    WorkPost update(String id, WorkPost post);
    void delete(String id);
    WorkPost getById(String id);

    List<WorkPost> getRecommendedPosts(int maxHours);

    WorkPost assignProjet(String workPostId, String projetId);
    WorkPost unassignProjet(String workPostId);
    List<WorkPost> getByProjet(String projetId);
}
