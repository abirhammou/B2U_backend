package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.spring.b2u.entity.WorkPost;

import java.util.List;

public interface WorkPostRepo extends MongoRepository<WorkPost, String> {

    List<WorkPost> findByEntrepriseId(String entrepriseId);
}
