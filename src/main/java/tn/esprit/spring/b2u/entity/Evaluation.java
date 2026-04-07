package tn.esprit.spring.b2u.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    private String idEval;

    // ── Who is being evaluated ──
    @NotBlank(message = "Student ID is required")
    private String idEtudiant;

    private String nomEtudiant;

    // ── Which project this evaluation belongs to ──
    @NotBlank(message = "Project ID is required")
    private String idProjet;

    private String nomProjet;

    // ── Which company made this evaluation ──
    private String idEntreprise;
    private String nomEntreprise;

    // ── Which team ──
    private String idEquipe;

    // ── Evaluation type ──
    @NotNull(message = "Evaluation type is required")
    private EvaluationType type; // PROFILE or PROJECT

    // ── Core scoring dimensions (0–100 each) ──
    @Min(0) @Max(100)
    private int technicalSkills;       // Compétences techniques

    @Min(0) @Max(100)
    private int communication;         // Communication

    @Min(0) @Max(100)
    private int projectExperience;     // Expérience projet

    @Min(0) @Max(100)
    private int problemSolving;        // Résolution de problèmes

    @Min(0) @Max(100)
    private int teamwork;              // Travail en équipe

    @Min(0) @Max(100)
    private int punctuality;           // Ponctualité & respect des délais

    @Min(0) @Max(100)
    private int creativity;            // Créativité & innovation

    // ── Computed overall score (weighted average) ──
    private Double overallScore;

    // ── AI Rank based on score ──
    private ScoreRank rank; // BRONZE, SILVER, GOLD, PLATINUM

    // ── Written feedback ──
    private String commentaire;

    // ── Recommendations from the evaluator ──
    private List<String> recommendations;

    // ── Strengths identified ──
    private List<String> strengths;

    // ── Areas to improve ──
    private List<String> improvements;

    // ── Interview reference (optional) ──
    private String idEntretien;

    // ── Timestamps ──
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Status ──
    private EvaluationStatus status; // DRAFT, SUBMITTED, VALIDATED

    // ── Enums ──
    public enum EvaluationType {
        PROFILE,   // EvaluationProfil — evaluates student profile
        PROJECT    // EvaluationProjet — evaluates project work
    }

    public enum ScoreRank {
        BRONZE,    // 0–49
        SILVER,    // 50–69
        GOLD,      // 70–84
        PLATINUM   // 85–100
    }

    public enum EvaluationStatus {
        DRAFT,
        SUBMITTED,
        VALIDATED
    }
}