package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.Etudiant;
import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.service.EtudiantService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/etudiant")
@RequiredArgsConstructor
@Tag(name = "Etudiant", description = "Gestion du profil étudiant")
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Obtenir mon profil étudiant")
    public ResponseEntity<?> getMyProfile(Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Etudiant etudiant = etudiantService.getByUserId(user.getId());
        if (etudiant == null) {
            return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "email", user.getEmail()
            ));
        }
        return ResponseEntity.ok(etudiant);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Obtenir le profil d'un étudiant par userId")
    public ResponseEntity<?> getByUserId(@PathVariable String userId) {
        Etudiant etudiant = etudiantService.getByUserId(userId);
        if (etudiant == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(etudiant);
    }

    @PutMapping("/me")
    @Operation(summary = "Mettre à jour mon profil étudiant")
    public ResponseEntity<Etudiant> updateMyProfile(Principal principal,
                                                     @RequestBody Etudiant data) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(etudiantService.upsert(user.getId(), data));
    }
}
