package tn.esprit.spring.b2u.controller;

import tn.esprit.spring.b2u.service.candidature.CandidatureService;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/candidatures")
@CrossOrigin(origins = "http://localhost:4200")
public class CandidatureController {

    @Autowired
    private CandidatureService candidatureService;

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

    @DeleteMapping("/{id}")
    public void deleteCandidature(@PathVariable String id) {
        candidatureService.deleteCandidature(id);
    }
}