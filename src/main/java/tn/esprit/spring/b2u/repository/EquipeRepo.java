package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.Equipe;

import java.util.List;

@Repository
public interface EquipeRepo extends MongoRepository<Equipe, String> {

    List<Equipe> findByEntrepriseId(String entrepriseId);
}
