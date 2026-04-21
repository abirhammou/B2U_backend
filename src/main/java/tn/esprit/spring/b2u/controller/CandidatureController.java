package tn.esprit.spring.b2u.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
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

    @PostMapping(consumes = "multipart/form-data")
    public CandidatureDTO createCandidature(
            @RequestPart("data") CandidatureDTO dto,
            @RequestPart("cv") MultipartFile cv,
            @RequestPart("lettre") MultipartFile lettre
    ) {
        // 🔍 LOGS DE DEBUG
        System.out.println("========== DÉBUT REQUÊTE ==========");
        System.out.println("DTO reçu: " + dto);
        System.out.println("Email: " + dto.getEmail());
        System.out.println("CV reçu: " + cv.getOriginalFilename());
        System.out.println("CV Content-Type: " + cv.getContentType());
        System.out.println("CV size: " + cv.getSize() + " bytes");
        System.out.println("Lettre reçue: " + lettre.getOriginalFilename());
        System.out.println("Lettre Content-Type: " + lettre.getContentType());
        System.out.println("Lettre size: " + lettre.getSize() + " bytes");
        System.out.println("===================================");

        return candidatureService.createCandidature(dto, cv, lettre);
    }
    @PostMapping("/test-upload")
    public String test(@RequestParam String name) {
        System.out.println("NAME = " + name);
        return "OK " + name;

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