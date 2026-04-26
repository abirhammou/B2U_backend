package tn.esprit.spring.b2u.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import tn.esprit.spring.b2u.service.candidature.CandidatureService;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.Valid;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidatureController {

    private final CandidatureService candidatureService;
    private final ObjectMapper objectMapper;

    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
        this.objectMapper = new ObjectMapper();
        // Register JavaTimeModule to handle LocalDate
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @GetMapping
    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureService.getAllCandidatures();
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public CandidatureDTO createCandidature(
            @RequestParam("data") String dtoJson,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("lettre") MultipartFile lettre,
            @RequestParam String projectId
    ) throws IOException {

        // ✅ Get bytes from MultipartFile
        byte[] cvBytes = cv.getBytes();
        byte[] lettreBytes = lettre.getBytes();

        // Parse JSON to DTO
        CandidatureDTO dto = objectMapper.readValue(dtoJson, CandidatureDTO.class);

        // Pass bytes to service
        return candidatureService.createCandidature(dto, cvBytes, lettreBytes, projectId);
    }
    @PutMapping("/{id}")
    public CandidatureDTO update(@PathVariable String id,
                                 @RequestBody CandidatureDTO dto) {
        return candidatureService.updateCandidature(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        candidatureService.deleteCandidature(id);
    }

    @GetMapping("/my")
    public List<CandidatureDTO> my(@RequestParam String email) {
        return candidatureService.getCandidaturesByEmail(email);
    }
// Ajouter dans CandidatureController

    @GetMapping("/project/{projectId}/ranking")
    public List<CandidatureDTO> getCandidatesRanking(@PathVariable String projectId) {
        return candidatureService.getCandidaturesByProject(projectId);
    }

    @GetMapping("/project/{projectId}/top/{limit}")
    public List<CandidatureDTO> getTopCandidates(@PathVariable String projectId,
                                                 @PathVariable int limit) {
        return candidatureService.getTopCandidatesForProject(projectId, limit);
    }

    @GetMapping("/recommended")
    public List<CandidatureDTO> getRecommendedCandidates() {
        return candidatureService.getRecommendedCandidates();
    }

    @GetMapping("/project/{projectId}/stats")
    public Map<String, Object> getMatchingStats(@PathVariable String projectId) {
        return candidatureService.getMatchingStats(projectId);
    }
}