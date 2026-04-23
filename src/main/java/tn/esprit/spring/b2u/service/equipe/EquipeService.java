package tn.esprit.spring.b2u.service.equipe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.EquipeDTO;
import tn.esprit.spring.b2u.entity.Equipe;
import tn.esprit.spring.b2u.repository.EquipeRepo;  // ← Utiliser EquipeRepo, pas EquipeRepository

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipeService implements IEquipeService {

    private final EquipeRepo equipeRepo;  // ← Changé ici

    @Override
    public Equipe ajouterEquipeFromDTO(EquipeDTO dto) {
        Equipe equipe = Equipe.builder()
                .nomMembresEquipe(dto.getNomMembresEquipe())
                .descriptionProfil(dto.getDescriptionProfil())
                .build();
        return equipeRepo.save(equipe);  // ← Utiliser equipeRepo
    }

    @Override
    public List<Equipe> getAllEquipes() {
        return equipeRepo.findAll();
    }

    @Override
    public Equipe getEquipeById(String id) {  // ← String, pas Long
        return equipeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipe non trouvée avec l'id: " + id));
    }

    @Override
    public Equipe modifierEquipe(Equipe equipe) {
        return equipeRepo.save(equipe);
    }

    @Override
    public void supprimerEquipe(String id) {
        equipeRepo.deleteById(id);
    }



    public List<Equipe> getEquipesByEntreprise(String entrepriseId) {
        return equipeRepo.findByEntrepriseId(entrepriseId);
    }


}






