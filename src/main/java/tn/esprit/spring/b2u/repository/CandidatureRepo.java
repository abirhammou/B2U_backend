package tn.esprit.spring.b2u.repository;

import tn.esprit.spring.b2u.entity.Candidature;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;



public interface CandidatureRepo extends MongoRepository<Candidature, String> {
    List<Candidature> findByEmail(String email);
}
