package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.entity.Etudiant;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import tn.esprit.spring.b2u.repository.EtudiantRepo;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.service.AiAssistantService;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "AI-powered features: project description, skill gap, candidature feedback")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;
    private final CandidatureRepo candidatureRepo;
    private final ProjetRepository projetRepository;
    private final EtudiantRepo etudiantRepo;
    private final UserRepository userRepository;

    // ── B: Generate project description + required skills ─────────────────
    @PostMapping("/generate-project")
    public ResponseEntity<Map<String, Object>> generateProject(@RequestBody Map<String, String> req) {
        String title = req.getOrDefault("title", "");
        String sector = req.getOrDefault("sector", "Technologie");
        return ResponseEntity.ok(aiAssistantService.generateProjectDescription(title, sector));
    }

    // ── C: Skill gap analysis ─────────────────────────────────────────────
    @PostMapping("/skill-gap")
    public ResponseEntity<Map<String, Object>> skillGap(@RequestBody Map<String, String> req,
                                                         Principal principal) {
        String projectId = req.get("projectId");
        Projet projet = projetRepository.findById(projectId)
            .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        List<String> projectSkills = projet.getAllRequiredSkills();

        List<String> studentSkills = List.of();
        if (principal != null) {
            Optional<User> userOpt = userRepository.findByEmail(principal.getName());
            if (userOpt.isPresent()) {
                Optional<Etudiant> etudiantOpt = etudiantRepo.findByUserId(userOpt.get().getId());
                if (etudiantOpt.isPresent() && etudiantOpt.get().getSkills() != null) {
                    studentSkills = etudiantOpt.get().getSkills();
                }
            }
        }

        return ResponseEntity.ok(aiAssistantService.analyzeSkillGap(studentSkills, projectSkills, projet.getTitle()));
    }

    // ── D: Candidature feedback for refused candidatures ──────────────────
    @PostMapping("/candidature-feedback/{candidatureId}")
    public ResponseEntity<Map<String, String>> candidatureFeedback(@PathVariable String candidatureId) {
        Candidature c = candidatureRepo.findById(candidatureId)
            .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        String name = (c.getPrenomCandidat() != null ? c.getPrenomCandidat() : "") + " "
                    + (c.getNomCandidat() != null ? c.getNomCandidat() : "");
        String feedback = aiAssistantService.generateCandidatureFeedback(
            name.trim(),
            c.getProjectTitle(),
            c.getScoreMatching(),
            c.getMatchingDetails(),
            c.getSpecialite()
        );

        return ResponseEntity.ok(Map.of("feedback", feedback));
    }
}
