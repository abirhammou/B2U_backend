package tn.esprit.spring.b2u.service.equipe;

import tn.esprit.spring.b2u.DTO.EquipeDTO;
import tn.esprit.spring.b2u.entity.Equipe;

import java.util.List;

public interface IEquipeService {

    Equipe ajouterEquipeFromDTO(EquipeDTO dto);
    List<Equipe> getAllEquipes();

    Equipe getEquipeById(String id);

    Equipe modifierEquipe(Equipe equipe);

    void supprimerEquipe(String id);




}
