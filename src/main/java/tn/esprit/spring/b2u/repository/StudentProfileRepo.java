package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.StudentProfile;
import java.util.Optional;

@Repository
public interface StudentProfileRepo extends MongoRepository<StudentProfile, String> {
    Optional<StudentProfile> findByUserId(String userId);
}