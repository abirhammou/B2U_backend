package tn.esprit.spring.b2u.service.candidature;

import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired
    private CandidatureRepo candidatureRepository;

    // ===== Convert Entity <-> DTO =====
    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setIdCandidature(c.getIdCandidature());
        dto.setNomCandidat(c.getNomCandidat());
        dto.setPrenomCandidat(c.getPrenomCandidat());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setAdresse(c.getAdresse());
        dto.setFormationActuelle(c.getFormationActuelle());
        dto.setSpecialite(c.getSpecialite());
        dto.setAnneeExperience(c.getAnneeExperience());
        dto.setDateCandidature(c.getDateCandidature());
        dto.setStatutCandidature(c.getStatutCandidature());
        dto.setCompetences(c.getCompetences());
        dto.setCvLien(c.getCvLien());
        dto.setLettreMotivation(c.getLettreMotivation());
        return dto;
    }
    public List<CandidatureDTO> getCandidaturesByEmail(String email) {
        return candidatureRepository.findByEmail(email)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private Candidature convertToEntity(CandidatureDTO dto) {
        Candidature c = new Candidature();
        c.setIdCandidature(dto.getIdCandidature());
        c.setNomCandidat(dto.getNomCandidat());
        c.setPrenomCandidat(dto.getPrenomCandidat());
        c.setEmail(dto.getEmail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        c.setFormationActuelle(dto.getFormationActuelle());
        c.setSpecialite(dto.getSpecialite());
        c.setAnneeExperience(dto.getAnneeExperience());
        c.setDateCandidature(dto.getDateCandidature());
        c.setStatutCandidature(dto.getStatutCandidature());
        c.setCompetences(dto.getCompetences());
        c.setCvLien(dto.getCvLien());
        c.setLettreMotivation(dto.getLettreMotivation());
        return c;
    }

    // ===== GET ALL =====
    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    // ===== CREATE =====
    public CandidatureDTO createCandidature(CandidatureDTO dto) {
        Candidature c = convertToEntity(dto);
        Candidature saved = candidatureRepository.save(c);
        System.out.println("✅ Candidature de " + saved.getNomCandidat() + " créée avec succès");
        return convertToDTO(saved);
    }

    // ===== UPDATE =====
    public CandidatureDTO updateCandidature(String id, CandidatureDTO dto) {
        Candidature existing = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature avec id " + id + " non trouvée"));

        // Mettre à jour les champs
        existing.setNomCandidat(dto.getNomCandidat());
        existing.setPrenomCandidat(dto.getPrenomCandidat());
        existing.setEmail(dto.getEmail());
        existing.setTelephone(dto.getTelephone());
        existing.setAdresse(dto.getAdresse());
        existing.setFormationActuelle(dto.getFormationActuelle());
        existing.setSpecialite(dto.getSpecialite());
        existing.setAnneeExperience(dto.getAnneeExperience());
        existing.setDateCandidature(dto.getDateCandidature());
        existing.setStatutCandidature(dto.getStatutCandidature());
        existing.setCompetences(dto.getCompetences());
        existing.setCvLien(dto.getCvLien());
        existing.setLettreMotivation(dto.getLettreMotivation());

        Candidature updated = candidatureRepository.save(existing);
        System.out.println(" Candidature avec id " + id + " mise à jour avec succès");
        return convertToDTO(updated);
    }

    // ===== DELETE =====
    public void deleteCandidature(String id) {
        if (!candidatureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidature avec id " + id + " non trouvée");
        }
        candidatureRepository.deleteById(id);
        System.out.println(" Candidature avec id " + id + " supprimée avec succès");
    }

}