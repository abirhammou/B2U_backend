package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.spring.b2u.entity.Etudiant;

import java.util.Optional;

public interface EtudiantRepo extends MongoRepository<Etudiant, String> {
    Optional<Etudiant> findByUserId(String userId);
}
