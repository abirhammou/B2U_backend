package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.Sprint;
import java.util.List;

@Repository
public interface SprintRepo extends MongoRepository<Sprint, String> {
    List<Sprint> findByProjetId(String projetId);
}