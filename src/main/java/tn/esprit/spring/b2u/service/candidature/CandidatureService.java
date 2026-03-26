package tn.esprit.spring.b2u.service.candidature;

import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired
    private CandidatureRepo candidatureRepository;

    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setIdCandidature(c.getIdCandidature());
        dto.setNomCandidat(c.getNomCandidat());
        dto.setPrenomCandidat(c.getPrenomCandidat());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setAdresse(c.getAdresse());
        dto.setDateNaissance(c.getDateNaissance());
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

    private Candidature convertToEntity(CandidatureDTO dto) {
        Candidature c = new Candidature();
        c.setIdCandidature(dto.getIdCandidature());
        c.setNomCandidat(dto.getNomCandidat());
        c.setPrenomCandidat(dto.getPrenomCandidat());
        c.setEmail(dto.getEmail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        c.setDateNaissance(dto.getDateNaissance());
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

    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CandidatureDTO createCandidature(CandidatureDTO dto) {
        Candidature c = convertToEntity(dto);
        Candidature saved = candidatureRepository.save(c);
        return convertToDTO(saved);
    }

    public CandidatureDTO updateCandidature(String id, CandidatureDTO dto) {
        Candidature c = convertToEntity(dto);
        c.setIdCandidature(id);
        Candidature updated = candidatureRepository.save(c);
        return convertToDTO(updated);
    }

    public void deleteCandidature(String id) {
        candidatureRepository.deleteById(id);
    }
}