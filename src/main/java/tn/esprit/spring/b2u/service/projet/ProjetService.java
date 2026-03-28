package tn.esprit.spring.b2u.service.projet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.repository.ProjetRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjetService implements IProjetService {

    private final ProjetRepository projetRepository;

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
        return projetRepository.save(projet);
    }

    @Override
    public Projet updateProjet(String id, Projet projet) {
        projet.setId(id);
        return projetRepository.save(projet);
    }

    @Override
    public void deleteProjet(String id) {
        projetRepository.deleteById(id);
    }
}