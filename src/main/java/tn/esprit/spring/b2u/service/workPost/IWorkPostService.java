package tn.esprit.spring.b2u.service.workPost;

import tn.esprit.spring.b2u.entity.WorkPost;

import java.util.List;

public interface IWorkPostService {

    WorkPost create(WorkPost post);

    List<WorkPost> getAll();

    List<WorkPost> getByEntreprise(String entrepriseId);

    WorkPost update(String id, WorkPost post);

    void delete(String id);

    // 🔥 logique métier
    List<WorkPost> getRecommendedPosts(int maxHours);
}
