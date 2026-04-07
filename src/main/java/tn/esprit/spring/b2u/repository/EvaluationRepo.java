package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.Evaluation;
import tn.esprit.spring.b2u.entity.Evaluation.EvaluationType;
import tn.esprit.spring.b2u.entity.Evaluation.ScoreRank;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepo extends MongoRepository<Evaluation, String> {

    // Find all evaluations for a student
    List<Evaluation> findByIdEtudiant(String idEtudiant);

    // Find all evaluations for a project
    List<Evaluation> findByIdProjet(String idProjet);

    // Find by student + type
    List<Evaluation> findByIdEtudiantAndType(String idEtudiant, EvaluationType type);

    // Find by company
    List<Evaluation> findByIdEntreprise(String idEntreprise);

    // Find by team
    List<Evaluation> findByIdEquipe(String idEquipe);

    // Find latest evaluation for a student
    Optional<Evaluation> findTopByIdEtudiantOrderByCreatedAtDesc(String idEtudiant);

    // Find by rank
    List<Evaluation> findByRank(ScoreRank rank);

    // Find by student + project (unique evaluation)
    Optional<Evaluation> findByIdEtudiantAndIdProjet(String idEtudiant, String idProjet);

    // Count evaluations per student
    long countByIdEtudiant(String idEtudiant);

    // Find all validated evaluations
    List<Evaluation> findByStatus(Evaluation.EvaluationStatus status);

    // Find students with score above threshold
    @Query("{ 'overallScore': { $gte: ?0 } }")
    List<Evaluation> findByOverallScoreGreaterThanEqual(double minScore);
}