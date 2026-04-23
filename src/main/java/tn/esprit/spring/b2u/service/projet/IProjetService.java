package tn.esprit.spring.b2u.service.projet;

import tn.esprit.spring.b2u.entity.Projet;
import java.util.List;
import java.util.Optional;

public interface IProjetService {
    List<Projet> getAllProjets();
    Optional<Projet> getProjetById(String id);
    Projet createProjet(Projet projet);
    Projet updateProjet(String id, Projet projet);
    void deleteProjet(String id);
}