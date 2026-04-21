package tn.esprit.spring.b2u.service.candidature;

import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.CandidatureRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired
    private CandidatureRepo candidatureRepository;

    // ===== Convert Entity -> DTO =====
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
    // ===== Convert DTO -> Entity =====
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
        return c;
    }

    // ===== GET BY EMAIL =====
    public List<CandidatureDTO> getCandidaturesByEmail(String email) {
        return candidatureRepository.findByEmail(email)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== GET ALL =====
    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== CREATE WITH PDF =====
    // ===== CREATE WITH PDF =====
    public CandidatureDTO createCandidature(CandidatureDTO dto,
                                            MultipartFile cv,
                                            MultipartFile lettre) {

        try {
            // ✅ Vérification PDF (garde ton code existant)
            if (cv == null || (!cv.getContentType().equals("application/pdf") && !cv.getContentType().equals("application/octet-stream"))) {
                throw new RuntimeException("Le CV doit être un fichier PDF");
            }

            if (lettre == null || (!lettre.getContentType().equals("application/pdf") && !lettre.getContentType().equals("application/octet-stream"))) {
                throw new RuntimeException("La lettre de motivation doit être un PDF");
            }

            // ✅ CORRECTION : Chemin absolu vers le dossier uploads à la racine du projet
            String uploadDir = System.getProperty("user.dir") + "/uploads/";

            // ✅ Créer dossier uploads s'il n'existe pas
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                System.out.println("Dossier uploads créé: " + created + " à " + uploadDir);
            }

            // ✅ Générer noms uniques
            String cvFileName = System.currentTimeMillis() + "_" + cv.getOriginalFilename();
            String lettreFileName = System.currentTimeMillis() + "_" + lettre.getOriginalFilename();

            // ✅ Chemins complets
            String cvPath = uploadDir + cvFileName;
            String lettrePath = uploadDir + lettreFileName;

            System.out.println("Sauvegarde CV dans: " + cvPath);
            System.out.println("Sauvegarde Lettre dans: " + lettrePath);

            // ✅ Sauvegarde fichiers
            cv.transferTo(new File(cvPath));
            lettre.transferTo(new File(lettrePath));

            // ✅ Sauvegarde DB (garde les chemins relatifs pour la base)
            Candidature c = convertToEntity(dto);
            c.setCvLien("uploads/" + cvFileName);  // Chemin relatif pour la BD
            c.setLettreMotivation("uploads/" + lettreFileName);

            Candidature saved = candidatureRepository.save(c);

            System.out.println("✅ Candidature créée avec fichiers PDF");

            return convertToDTO(saved);

        } catch (IOException e) {
            e.printStackTrace();  // Ajoute ceci pour voir l'erreur complète
            throw new RuntimeException("Erreur lors de l'upload des fichiers: " + e.getMessage(), e);
        }
    }
    // ===== UPDATE =====
    public CandidatureDTO updateCandidature(String id, CandidatureDTO dto) {

        Candidature existing = candidatureRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Candidature avec id " + id + " non trouvée"));

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

        Candidature updated = candidatureRepository.save(existing);

        System.out.println(" Candidature mise à jour");

        return convertToDTO(updated);
    }

    // ===== DELETE =====
    public void deleteCandidature(String id) {
        if (!candidatureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidature avec id " + id + " non trouvée");
        }
        candidatureRepository.deleteById(id);
        System.out.println(" Candidature supprimée");
    }
}