package tn.esprit.spring.b2u.service.projet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import tn.esprit.spring.b2u.repository.ProjetRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjetService implements IProjetService {

    private final ProjetRepository projetRepository;
    private final CandidatureRepo candidatureRepository;

    @Override
    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }

    @Override
    public Optional<Projet> getProjetById(String id) {
        return projetRepository.findById(id);
    }

    @Override
    public Projet createProjet(Projet projet) {
        projet.setApplicantsCount(0);
        if (projet.getType() == null || projet.getType().isBlank()) {
            projet.setType("PROJET");
        }
        if (projet.getCreatedAt() == null) {
            projet.setCreatedAt(new Date());
        }
        return projetRepository.save(projet);
    }

    @Override
    public Projet updateProjet(String id, Projet projet) {
        projet.setId(id);
        projet.setApplicantsCount((int) candidatureRepository.countByProjectId(id));
        return projetRepository.save(projet);
    }

    @Override
    public void deleteProjet(String id) {
        projetRepository.deleteById(id);
    }
}
