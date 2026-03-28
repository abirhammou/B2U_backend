package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.DTO.EquipeDTO;
import tn.esprit.spring.b2u.entity.Equipe;

import tn.esprit.spring.b2u.service.equipe.IEquipeService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equipe")
@RequiredArgsConstructor
public class EquipeController {

    private final IEquipeService equipeService;

    @PostMapping("/add")
    public ResponseEntity<?> ajouterEquipe(@Valid @RequestBody EquipeDTO dto) {

        Equipe equipe = equipeService.ajouterEquipeFromDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(equipe);
    }

    // Afficher toutes les équipes
    @GetMapping("/all")
    public List<Equipe> getAllEquipes() {
        return equipeService.getAllEquipes();
    }

    // Afficher une équipe par ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipeById(@PathVariable String id) {
        Equipe equipe = equipeService.getEquipeById(id);
        if (equipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Équipe introuvable avec l'id: " + id);
        }
        return ResponseEntity.ok(equipe);
    }

    // Modifier une équipe avec validation
    @PutMapping("/update")
    public ResponseEntity<?> modifierEquipe(@Valid @RequestBody Equipe equipe,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing
                    ));

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("error", "Validation échouée");
            errorResponse.put("errors", errors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Equipe updatedEquipe = equipeService.modifierEquipe(equipe);
        return ResponseEntity.ok(updatedEquipe);
    }

    // Supprimer une équipe
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> supprimerEquipe(@PathVariable String id) {
        Equipe equipe = equipeService.getEquipeById(id);
        if (equipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Équipe introuvable avec l'id: " + id);
        }
        equipeService.supprimerEquipe(id);
        return ResponseEntity.ok("Équipe supprimée avec succès");
    }
}