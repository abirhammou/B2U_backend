package tn.esprit.spring.b2u.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.DTO.EntrepriseDTO;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.service.entreprise.IEntrepriseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/entreprise")
@RequiredArgsConstructor
public class EntrepriseController {

    private final IEntrepriseService enterpriseService;

    @GetMapping("/getAll")
    public List<Entreprise> getAllEnterprises() {
        return enterpriseService.getAllEnterprises();
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<Entreprise> getEnterpriseById(@PathVariable String id) {
        return enterpriseService.getEnterpriseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> createEnterprise(@Valid @RequestBody EntrepriseDTO enterpriseDTO) {

        enterpriseService.createEnterprise(enterpriseDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Entreprise added successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateEnterprise(@PathVariable String id,
                                                                @Valid @RequestBody EntrepriseDTO enterpriseDTO) {

        enterpriseService.updateEnterprise(id, enterpriseDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Entreprise updated successfully");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteEnterprise(@PathVariable String id) {

        enterpriseService.deleteEnterprise(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Entreprise deleted successfully");

        return ResponseEntity.ok(response);
    }
}
