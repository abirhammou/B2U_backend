package tn.esprit.spring.b2u.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import tn.esprit.spring.b2u.entity.Evaluation.EvaluationType;
import tn.esprit.spring.b2u.entity.Evaluation.EvaluationStatus;
import tn.esprit.spring.b2u.entity.Evaluation.ScoreRank;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationDTO {

    private String idEval;

    @NotBlank(message = "Student ID is required")
    private String idEtudiant;
    private String nomEtudiant;

    @NotBlank(message = "Project ID is required")
    private String idProjet;
    private String nomProjet;

    private String idEntreprise;
    private String nomEntreprise;
    private String idEquipe;

    @NotNull(message = "Type is required")
    private EvaluationType type;

    @Min(0) @Max(100)
    private int technicalSkills;

    @Min(0) @Max(100)
    private int communication;

    @Min(0) @Max(100)
    private int projectExperience;

    @Min(0) @Max(100)
    private int problemSolving;

    @Min(0) @Max(100)
    private int teamwork;

    @Min(0) @Max(100)
    private int punctuality;

    @Min(0) @Max(100)
    private int creativity;

    // Computed — returned by backend, not sent by client
    private Double overallScore;
    private ScoreRank rank;

    private String commentaire;
    private List<String> recommendations;
    private List<String> strengths;
    private List<String> improvements;

    private String idEntretien;
    private EvaluationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}