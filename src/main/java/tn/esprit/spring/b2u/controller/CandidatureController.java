package tn.esprit.spring.b2u.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import tn.esprit.spring.b2u.service.candidature.CandidatureService;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidatureController {

    private final CandidatureService candidatureService;

    // ✅ injection propre via constructeur
    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    @GetMapping
    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureService.getAllCandidatures();
    }

    @PostMapping
    public CandidatureDTO createCandidature(@Valid @RequestBody CandidatureDTO dto) {
        return candidatureService.createCandidature(dto);
    }

    @PutMapping("/{id}")
    public CandidatureDTO updateCandidature(@PathVariable String id,
                                            @Valid @RequestBody CandidatureDTO dto) {
        return candidatureService.updateCandidature(id, dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        candidatureService.deleteCandidature(id);
    }

    @GetMapping("/my")
    public List<CandidatureDTO> getMyCandidatures(@RequestParam String email) {
        return candidatureService.getCandidaturesByEmail(email);
    }

}