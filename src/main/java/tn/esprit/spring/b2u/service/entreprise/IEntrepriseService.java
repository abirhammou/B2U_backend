package tn.esprit.spring.b2u.service.entreprise;

import tn.esprit.spring.b2u.DTO.EntrepriseDTO;
import tn.esprit.spring.b2u.entity.Entreprise;

import java.util.List;
import java.util.Optional;

public interface IEntrepriseService {

    List<Entreprise> getAllEnterprises();
    Optional<Entreprise> getEnterpriseById(String id);
    void createEnterprise(EntrepriseDTO dto);
    Entreprise updateEnterprise(String id, EntrepriseDTO dto);
    void deleteEnterprise(String id);
}
