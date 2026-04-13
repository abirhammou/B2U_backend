package tn.esprit.spring.b2u.repository;


import tn.esprit.spring.b2u.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {


    Optional<User> findByEmail(String email);
}