package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.DTO.EvaluationDTO;
import tn.esprit.spring.b2u.entity.Evaluation.EvaluationType;
import tn.esprit.spring.b2u.service.IEvaluationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Evaluation", description = "AI Scoring & Evaluation management")
public class EvaluationController {

    private final IEvaluationService evaluationService;

    // ── CREATE ──
    @PostMapping
    @Operation(summary = "Create a new evaluation")
    public ResponseEntity<EvaluationDTO> create(
            @Valid @RequestBody EvaluationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluationService.createEvaluation(dto));
    }

    // ── READ ALL ──
    @GetMapping
    @Operation(summary = "Get all evaluations (Admin)")
    public ResponseEntity<List<EvaluationDTO>> getAll() {
        return ResponseEntity.ok(evaluationService.getAllEvaluations());
    }

    // ── READ BY ID ──
    @GetMapping("/{id}")
    @Operation(summary = "Get evaluation by ID")
    public ResponseEntity<EvaluationDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(evaluationService.getEvaluationById(id));
    }

    // ── UPDATE ──
    @PutMapping("/{id}")
    @Operation(summary = "Update an evaluation")
    public ResponseEntity<EvaluationDTO> update(
            @PathVariable String id,
            @Valid @RequestBody EvaluationDTO dto) {
        return ResponseEntity.ok(evaluationService.updateEvaluation(id, dto));
    }

    // ── DELETE ──
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an evaluation (Admin)")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }

    // ── GET BY STUDENT ──
    @GetMapping("/student/{idEtudiant}")
    @Operation(summary = "Get all evaluations for a student")
    public ResponseEntity<List<EvaluationDTO>> getByStudent(
            @PathVariable String idEtudiant) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByStudent(idEtudiant));
    }

    // ── GET LATEST FOR STUDENT (used by AI Score page) ──
    @GetMapping("/student/{idEtudiant}/latest")
    @Operation(summary = "Get latest evaluation for a student — used by AI Score page")
    public ResponseEntity<EvaluationDTO> getLatest(
            @PathVariable String idEtudiant) {
        return ResponseEntity.ok(evaluationService.getLatestEvaluationForStudent(idEtudiant));
    }

    // ── GET BY PROJECT ──
    @GetMapping("/project/{idProjet}")
    @Operation(summary = "Get all evaluations for a project")
    public ResponseEntity<List<EvaluationDTO>> getByProject(
            @PathVariable String idProjet) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByProject(idProjet));
    }

    // ── GET BY COMPANY ──
    @GetMapping("/company/{idEntreprise}")
    @Operation(summary = "Get all evaluations submitted by a company")
    public ResponseEntity<List<EvaluationDTO>> getByCompany(
            @PathVariable String idEntreprise) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByCompany(idEntreprise));
    }

    // ── GET BY TYPE ──
    @GetMapping("/student/{idEtudiant}/type/{type}")
    @Operation(summary = "Get evaluations by student and type (PROFILE or PROJECT)")
    public ResponseEntity<List<EvaluationDTO>> getByType(
            @PathVariable String idEtudiant,
            @PathVariable EvaluationType type) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByType(idEtudiant, type));
    }

    // ── VALIDATE ──
    @PatchMapping("/{id}/validate")
    @Operation(summary = "Validate an evaluation (Admin)")
    public ResponseEntity<EvaluationDTO> validate(@PathVariable String id) {
        return ResponseEntity.ok(evaluationService.validateEvaluation(id));
    }

    // ── PLATFORM STATISTICS (Admin) ──
    @GetMapping("/stats/platform")
    @Operation(summary = "Get platform-wide evaluation statistics (Admin)")
    public ResponseEntity<Map<String, Object>> getPlatformStats() {
        return ResponseEntity.ok(evaluationService.getPlatformStatistics());
    }

    // ── STUDENT STATISTICS ──
    @GetMapping("/stats/student/{idEtudiant}")
    @Operation(summary = "Get evaluation statistics for a specific student")
    public ResponseEntity<Map<String, Object>> getStudentStats(
            @PathVariable String idEtudiant) {
        return ResponseEntity.ok(evaluationService.getStudentStatistics(idEtudiant));
    }
}