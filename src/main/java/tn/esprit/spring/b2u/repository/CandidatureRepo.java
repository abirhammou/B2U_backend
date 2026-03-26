package tn.esprit.spring.b2u.repository;

import tn.esprit.spring.b2u.entity.Candidature;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CandidatureRepo extends MongoRepository<Candidature, String> {

}