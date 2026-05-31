package tn.esprit.spring.b2u.service.candidature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.service.CvAnalysisService;
import tn.esprit.spring.b2u.service.InterviewQuestionAiService;
import tn.esprit.spring.b2u.service.notification.StudentNotificationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CandidatureServiceTest {

    @Mock
    private CandidatureRepo candidatureRepository;

    @Mock
    private ProjetRepository projectRepository;

    @Mock
    private CvAnalysisService cvAnalysisService;

    @Mock
    private InterviewQuestionAiService interviewQuestionAiService;

    @Mock
    private StudentNotificationService studentNotificationService;

    @InjectMocks
    private CandidatureService service;

    @Test
    void createCandidature_enrichitLaCandidatureAvecLeProjetEtMetAJourLeCompteur() {
        Projet projet = projet("project-1", "Plateforme B2U", "PROJET", "company-1", "B2U Company");
        CandidatureDTO dto = candidatureDto("project-1", "Etudiant", 67);

        when(projectRepository.findById("project-1")).thenReturn(Optional.of(projet));
        when(candidatureRepository.save(any(Candidature.class))).thenAnswer(invocation -> {
            Candidature saved = invocation.getArgument(0);
            saved.setIdCandidature("cand-1");
            return saved;
        });
        when(candidatureRepository.countByProjectId("project-1")).thenReturn(1L);

        CandidatureDTO result = service.createCandidature(dto);

        assertEquals("cand-1", result.getIdCandidature());
        assertEquals("Plateforme B2U", result.getProjectTitle());
        assertEquals("B2U Company", result.getCompanyName());
        assertEquals("En cours", result.getStatutCandidature());

        ArgumentCaptor<Candidature> captor = ArgumentCaptor.forClass(Candidature.class);
        verify(candidatureRepository).save(captor.capture());
        assertEquals("company-1", captor.getValue().getCompanyId());
        assertEquals("Angular", captor.getValue().getCompetences().get(0));

        verify(projectRepository).save(projet);
        assertEquals(1, projet.getApplicantsCount());
    }

    @Test
    void getCandidaturesByCompany_dedoublonneParIdEtTrieParScoreDecroissant() {
        Candidature faible = candidature("cand-1", "company-1", "B2U Company", 35);
        Candidature fort = candidature("cand-2", "company-1", "B2U Company", 91);

        when(candidatureRepository.findByCompanyId("company-1")).thenReturn(List.of(faible, fort));
        when(candidatureRepository.findByCompanyName("company-1")).thenReturn(List.of(fort));

        List<CandidatureDTO> result = service.getCandidaturesByCompany("company-1");

        assertEquals(2, result.size());
        assertEquals("cand-2", result.get(0).getIdCandidature());
        assertEquals(91, result.get(0).getScoreMatching());
        assertEquals("cand-1", result.get(1).getIdCandidature());
    }

    @Test
    void deleteCandidature_supprimeEtRafraichitLeNombreDeCandidatsDuProjet() {
        Candidature candidature = candidature("cand-1", "company-1", "B2U Company", 72);
        candidature.setProjectId("project-1");
        Projet projet = projet("project-1", "Projet Test", "PROJET", "company-1", "B2U Company");

        when(candidatureRepository.findById("cand-1")).thenReturn(Optional.of(candidature));
        when(projectRepository.findById("project-1")).thenReturn(Optional.of(projet));
        when(candidatureRepository.countByProjectId("project-1")).thenReturn(3L);

        service.deleteCandidature("cand-1");

        verify(candidatureRepository).deleteById("cand-1");
        verify(projectRepository).save(projet);
        assertEquals(3, projet.getApplicantsCount());
    }

    @Test
    void deleteCandidature_lanceUneErreurSiLaCandidatureNExistePas() {
        when(candidatureRepository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteCandidature("missing"));
        verify(candidatureRepository, never()).deleteById("missing");
    }

    @Test
    void getMatchingStats_calculeLesStatistiquesPourUnProjet() {
        Projet projet = projet("project-1", "Projet Test", "PROJET", "company-1", "B2U Company");
        Candidature excellent = candidature("cand-1", "company-1", "B2U Company", 88);
        Candidature bon = candidature("cand-2", "company-1", "B2U Company", 70);
        Candidature moyen = candidature("cand-3", "company-1", "B2U Company", 55);
        Candidature faible = candidature("cand-4", "company-1", "B2U Company", 20);
        excellent.setProjectId("project-1");
        bon.setProjectId("project-1");
        moyen.setProjectId("project-1");
        faible.setProjectId("project-1");

        when(projectRepository.findById("project-1")).thenReturn(Optional.of(projet));
        when(candidatureRepository.findByProjectId("project-1"))
                .thenReturn(List.of(faible, moyen, excellent, bon));

        var stats = service.getMatchingStats("project-1");

        assertEquals(4, stats.get("totalCandidates"));
        assertEquals(1L, stats.get("excellentCount"));
        assertEquals(1L, stats.get("goodCount"));
        assertEquals(1L, stats.get("averageCount"));
        assertEquals(1L, stats.get("weakCount"));
        assertEquals(58.25, (double) stats.get("averageScore"), 0.001);
        assertEquals(88, stats.get("topScore"));
    }

    private static CandidatureDTO candidatureDto(String projectId, String nom, int score) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setNomCandidat(nom);
        dto.setPrenomCandidat("Test");
        dto.setEmail("student@b2u.tn");
        dto.setTelephone("22111222");
        dto.setAdresse("Tunis");
        dto.setFormationActuelle("Master");
        dto.setSpecialite("DevOps");
        dto.setAnneeExperience(1);
        dto.setDateCandidature(LocalDate.now());
        dto.setStatutCandidature("En cours");
        dto.setProjectId(projectId);
        dto.setCompetences(List.of("Angular", "Spring Boot"));
        dto.setScoreMatching(score);
        return dto;
    }

    private static Candidature candidature(String id, String companyId, String companyName, int score) {
        Candidature candidature = new Candidature();
        candidature.setIdCandidature(id);
        candidature.setNomCandidat("Nom " + id);
        candidature.setPrenomCandidat("Prenom " + id);
        candidature.setEmail(id + "@b2u.tn");
        candidature.setTelephone("22111222");
        candidature.setStatutCandidature("En cours");
        candidature.setCompanyId(companyId);
        candidature.setCompanyName(companyName);
        candidature.setProjectId("project-1");
        candidature.setProjectTitle("Projet Test");
        candidature.setScoreMatching(score);
        candidature.setCompetences(List.of("Angular"));
        return candidature;
    }

    private static Projet projet(String id, String title, String type, String companyId, String companyName) {
        Projet projet = new Projet();
        projet.setId(id);
        projet.setTitle(title);
        projet.setType(type);
        projet.setCompanyId(companyId);
        projet.setCompanyName(companyName);
        projet.setTechnologies(List.of("Angular", "Spring Boot"));
        return projet;
    }
}
