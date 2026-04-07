package tn.esprit.spring.b2u.service;

import tn.esprit.spring.b2u.DTO.EvaluationDTO;
import tn.esprit.spring.b2u.entity.Evaluation.EvaluationType;
import tn.esprit.spring.b2u.entity.Evaluation.ScoreRank;

import java.util.List;
import java.util.Map;

public interface IEvaluationService {

    // ── CRUD ──
    EvaluationDTO createEvaluation(EvaluationDTO dto);
    EvaluationDTO updateEvaluation(String id, EvaluationDTO dto);
    void deleteEvaluation(String id);
    EvaluationDTO getEvaluationById(String id);
    List<EvaluationDTO> getAllEvaluations();

    // ── Business queries ──
    List<EvaluationDTO> getEvaluationsByStudent(String idEtudiant);
    List<EvaluationDTO> getEvaluationsByProject(String idProjet);
    List<EvaluationDTO> getEvaluationsByCompany(String idEntreprise);
    List<EvaluationDTO> getEvaluationsByType(String idEtudiant, EvaluationType type);
    EvaluationDTO getLatestEvaluationForStudent(String idEtudiant);

    // ── Scoring logic ──
    double computeOverallScore(EvaluationDTO dto);
    ScoreRank computeRank(double score);

    // ── Validation ──
    EvaluationDTO validateEvaluation(String id);

    // ── Statistics (for admin dashboard) ──
    Map<String, Object> getPlatformStatistics();
    Map<String, Object> getStudentStatistics(String idEtudiant);

    // ── AI Scoring ──
    List<String> generateRecommendations(EvaluationDTO dto);
    List<String> generateStrengths(EvaluationDTO dto);
    List<String> generateImprovements(EvaluationDTO dto);
}