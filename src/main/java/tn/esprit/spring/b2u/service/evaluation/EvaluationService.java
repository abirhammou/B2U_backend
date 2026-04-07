package tn.esprit.spring.b2u.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.EvaluationDTO;
import tn.esprit.spring.b2u.entity.Evaluation;
import tn.esprit.spring.b2u.entity.Evaluation.*;
import tn.esprit.spring.b2u.repository.EvaluationRepo;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService implements IEvaluationService {

    private final EvaluationRepo evaluationRepo;

    // ── Weights for overall score calculation ──
    private static final Map<String, Double> WEIGHTS = Map.of(
            "technicalSkills",    0.25,
            "communication",      0.15,
            "projectExperience",  0.20,
            "problemSolving",     0.15,
            "teamwork",           0.10,
            "punctuality",        0.10,
            "creativity",         0.05
    );

    // ────────────────────────────────────────
    // CRUD
    // ────────────────────────────────────────

    @Override
    public EvaluationDTO createEvaluation(EvaluationDTO dto) {
        // Compute score and rank before saving
        double score = computeOverallScore(dto);
        ScoreRank rank = computeRank(score);

        dto.setOverallScore(score);
        dto.setRank(rank);
        dto.setStatus(EvaluationStatus.SUBMITTED);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        // Auto-generate AI recommendations if not provided
        if (dto.getRecommendations() == null || dto.getRecommendations().isEmpty()) {
            dto.setRecommendations(generateRecommendations(dto));
        }
        if (dto.getStrengths() == null || dto.getStrengths().isEmpty()) {
            dto.setStrengths(generateStrengths(dto));
        }
        if (dto.getImprovements() == null || dto.getImprovements().isEmpty()) {
            dto.setImprovements(generateImprovements(dto));
        }

        Evaluation entity = toEntity(dto);
        Evaluation saved = evaluationRepo.save(entity);
        log.info("Evaluation created with id: {}", saved.getIdEval());
        return toDTO(saved);
    }

    @Override
    public EvaluationDTO updateEvaluation(String id, EvaluationDTO dto) {
        Evaluation existing = evaluationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found: " + id));

        // Recompute score on update
        double score = computeOverallScore(dto);
        dto.setOverallScore(score);
        dto.setRank(computeRank(score));
        dto.setIdEval(id);
        dto.setCreatedAt(existing.getCreatedAt());
        dto.setUpdatedAt(LocalDateTime.now());
        dto.setRecommendations(generateRecommendations(dto));
        dto.setStrengths(generateStrengths(dto));
        dto.setImprovements(generateImprovements(dto));

        Evaluation updated = evaluationRepo.save(toEntity(dto));
        return toDTO(updated);
    }

    @Override
    public void deleteEvaluation(String id) {
        if (!evaluationRepo.existsById(id)) {
            throw new RuntimeException("Evaluation not found: " + id);
        }
        evaluationRepo.deleteById(id);
        log.info("Evaluation deleted: {}", id);
    }

    @Override
    public EvaluationDTO getEvaluationById(String id) {
        return evaluationRepo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Evaluation not found: " + id));
    }

    @Override
    public List<EvaluationDTO> getAllEvaluations() {
        return evaluationRepo.findAll()
                .stream().map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ────────────────────────────────────────
    // Business Queries
    // ────────────────────────────────────────

    @Override
    public List<EvaluationDTO> getEvaluationsByStudent(String idEtudiant) {
        return evaluationRepo.findByIdEtudiant(idEtudiant)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByProject(String idProjet) {
        return evaluationRepo.findByIdProjet(idProjet)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByCompany(String idEntreprise) {
        return evaluationRepo.findByIdEntreprise(idEntreprise)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<EvaluationDTO> getEvaluationsByType(String idEtudiant, EvaluationType type) {
        return evaluationRepo.findByIdEtudiantAndType(idEtudiant, type)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public EvaluationDTO getLatestEvaluationForStudent(String idEtudiant) {
        return evaluationRepo.findTopByIdEtudiantOrderByCreatedAtDesc(idEtudiant)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("No evaluation found for student: " + idEtudiant));
    }

    @Override
    public EvaluationDTO validateEvaluation(String id) {
        Evaluation eval = evaluationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found: " + id));
        eval.setStatus(EvaluationStatus.VALIDATED);
        eval.setUpdatedAt(LocalDateTime.now());
        return toDTO(evaluationRepo.save(eval));
    }

    // ────────────────────────────────────────
    // Scoring Logic
    // ────────────────────────────────────────

    @Override
    public double computeOverallScore(EvaluationDTO dto) {
        double score =
                dto.getTechnicalSkills()    * WEIGHTS.get("technicalSkills")   +
                        dto.getCommunication()       * WEIGHTS.get("communication")      +
                        dto.getProjectExperience()   * WEIGHTS.get("projectExperience")  +
                        dto.getProblemSolving()      * WEIGHTS.get("problemSolving")     +
                        dto.getTeamwork()            * WEIGHTS.get("teamwork")           +
                        dto.getPunctuality()         * WEIGHTS.get("punctuality")        +
                        dto.getCreativity()          * WEIGHTS.get("creativity");

        // Round to 2 decimal places
        return Math.round(score * 100.0) / 100.0;
    }

    @Override
    public ScoreRank computeRank(double score) {
        if (score >= 85) return ScoreRank.PLATINUM;
        if (score >= 70) return ScoreRank.GOLD;
        if (score >= 50) return ScoreRank.SILVER;
        return ScoreRank.BRONZE;
    }

    // ────────────────────────────────────────
    // AI Recommendations Generation
    // ────────────────────────────────────────

    @Override
    public List<String> generateRecommendations(EvaluationDTO dto) {
        List<String> recs = new ArrayList<>();

        if (dto.getTechnicalSkills() < 60)
            recs.add("Focus on improving core technical skills — consider online certifications");
        if (dto.getCommunication() < 60)
            recs.add("Practice technical writing and presentation skills");
        if (dto.getProjectExperience() < 60)
            recs.add("Complete at least 2 more real-world projects to boost experience score");
        if (dto.getProblemSolving() < 60)
            recs.add("Work on algorithmic challenges on platforms like LeetCode or HackerRank");
        if (dto.getTeamwork() < 60)
            recs.add("Participate in collaborative open-source projects to improve teamwork");
        if (dto.getPunctuality() < 60)
            recs.add("Work on time management — use project tracking tools like Jira or Trello");
        if (dto.getCreativity() < 60)
            recs.add("Explore design thinking workshops and creative problem-solving methods");

        if (recs.isEmpty())
            recs.add("Excellent profile! Keep maintaining high standards and mentor others");

        return recs;
    }

    @Override
    public List<String> generateStrengths(EvaluationDTO dto) {
        List<String> strengths = new ArrayList<>();

        if (dto.getTechnicalSkills() >= 75)   strengths.add("Strong technical foundation");
        if (dto.getCommunication() >= 75)      strengths.add("Clear and effective communicator");
        if (dto.getProjectExperience() >= 75)  strengths.add("Solid real-world project experience");
        if (dto.getProblemSolving() >= 75)     strengths.add("Excellent analytical and problem-solving skills");
        if (dto.getTeamwork() >= 75)           strengths.add("Highly collaborative team player");
        if (dto.getPunctuality() >= 75)        strengths.add("Reliable and deadline-driven");
        if (dto.getCreativity() >= 75)         strengths.add("Creative thinker with innovative approach");

        if (strengths.isEmpty()) strengths.add("Showing potential — keep developing your skills");
        return strengths;
    }

    @Override
    public List<String> generateImprovements(EvaluationDTO dto) {
        List<String> improvements = new ArrayList<>();

        // Find the 3 lowest scoring dimensions
        Map<String, Integer> scores = new LinkedHashMap<>();
        scores.put("Technical Skills",    dto.getTechnicalSkills());
        scores.put("Communication",        dto.getCommunication());
        scores.put("Project Experience",   dto.getProjectExperience());
        scores.put("Problem Solving",      dto.getProblemSolving());
        scores.put("Teamwork",             dto.getTeamwork());
        scores.put("Punctuality",          dto.getPunctuality());
        scores.put("Creativity",           dto.getCreativity());

        scores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .forEach(e -> improvements.add(e.getKey() + " (current: " + e.getValue() + "/100)"));

        return improvements;
    }

    // ────────────────────────────────────────
    // Statistics
    // ────────────────────────────────────────

    @Override
    public Map<String, Object> getPlatformStatistics() {
        List<Evaluation> all = evaluationRepo.findAll();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalEvaluations", all.size());
        stats.put("averageScore",
                all.stream().mapToDouble(Evaluation::getOverallScore).average().orElse(0));
        stats.put("platinumCount",
                all.stream().filter(e -> e.getRank() == ScoreRank.PLATINUM).count());
        stats.put("goldCount",
                all.stream().filter(e -> e.getRank() == ScoreRank.GOLD).count());
        stats.put("silverCount",
                all.stream().filter(e -> e.getRank() == ScoreRank.SILVER).count());
        stats.put("bronzeCount",
                all.stream().filter(e -> e.getRank() == ScoreRank.BRONZE).count());
        stats.put("validatedCount",
                all.stream().filter(e -> e.getStatus() == EvaluationStatus.VALIDATED).count());

        return stats;
    }

    @Override
    public Map<String, Object> getStudentStatistics(String idEtudiant) {
        List<Evaluation> evals = evaluationRepo.findByIdEtudiant(idEtudiant);
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalEvaluations", evals.size());
        stats.put("averageScore",
                evals.stream().mapToDouble(Evaluation::getOverallScore).average().orElse(0));
        stats.put("bestScore",
                evals.stream().mapToDouble(Evaluation::getOverallScore).max().orElse(0));
        stats.put("latestRank",
                evals.stream().max(Comparator.comparing(Evaluation::getCreatedAt))
                        .map(e -> e.getRank().name()).orElse("N/A"));
        stats.put("avgTechnicalSkills",
                evals.stream().mapToInt(Evaluation::getTechnicalSkills).average().orElse(0));
        stats.put("avgCommunication",
                evals.stream().mapToInt(Evaluation::getCommunication).average().orElse(0));
        stats.put("avgProjectExperience",
                evals.stream().mapToInt(Evaluation::getProjectExperience).average().orElse(0));
        stats.put("avgProblemSolving",
                evals.stream().mapToInt(Evaluation::getProblemSolving).average().orElse(0));

        return stats;
    }

    // ────────────────────────────────────────
    // Mapper helpers
    // ────────────────────────────────────────

    private Evaluation toEntity(EvaluationDTO dto) {
        return Evaluation.builder()
                .idEval(dto.getIdEval())
                .idEtudiant(dto.getIdEtudiant())
                .nomEtudiant(dto.getNomEtudiant())
                .idProjet(dto.getIdProjet())
                .nomProjet(dto.getNomProjet())
                .idEntreprise(dto.getIdEntreprise())
                .nomEntreprise(dto.getNomEntreprise())
                .idEquipe(dto.getIdEquipe())
                .type(dto.getType())
                .technicalSkills(dto.getTechnicalSkills())
                .communication(dto.getCommunication())
                .projectExperience(dto.getProjectExperience())
                .problemSolving(dto.getProblemSolving())
                .teamwork(dto.getTeamwork())
                .punctuality(dto.getPunctuality())
                .creativity(dto.getCreativity())
                .overallScore(dto.getOverallScore())
                .rank(dto.getRank())
                .commentaire(dto.getCommentaire())
                .recommendations(dto.getRecommendations())
                .strengths(dto.getStrengths())
                .improvements(dto.getImprovements())
                .idEntretien(dto.getIdEntretien())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private EvaluationDTO toDTO(Evaluation e) {
        return EvaluationDTO.builder()
                .idEval(e.getIdEval())
                .idEtudiant(e.getIdEtudiant())
                .nomEtudiant(e.getNomEtudiant())
                .idProjet(e.getIdProjet())
                .nomProjet(e.getNomProjet())
                .idEntreprise(e.getIdEntreprise())
                .nomEntreprise(e.getNomEntreprise())
                .idEquipe(e.getIdEquipe())
                .type(e.getType())
                .technicalSkills(e.getTechnicalSkills())
                .communication(e.getCommunication())
                .projectExperience(e.getProjectExperience())
                .problemSolving(e.getProblemSolving())
                .teamwork(e.getTeamwork())
                .punctuality(e.getPunctuality())
                .creativity(e.getCreativity())
                .overallScore(e.getOverallScore())
                .rank(e.getRank())
                .commentaire(e.getCommentaire())
                .recommendations(e.getRecommendations())
                .strengths(e.getStrengths())
                .improvements(e.getImprovements())
                .idEntretien(e.getIdEntretien())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}