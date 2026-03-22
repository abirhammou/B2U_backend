package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.Entreprise;

@Repository
public interface EntrepriseRepo extends MongoRepository<Entreprise, String> {

    boolean existsByName(String name);

    boolean existsByPhone(String phone);

    boolean existsByNameAndIdNot(String name, String id);
    boolean existsByPhoneAndIdNot(String phone, String id);
}
