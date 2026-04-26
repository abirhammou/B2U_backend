package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.Projet;

@Repository
public interface ProjetRepository extends MongoRepository<Projet, String> {
}