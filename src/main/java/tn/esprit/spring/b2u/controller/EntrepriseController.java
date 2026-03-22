package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Entreprise", description = "Endpoints for managing enterprises")
public class EntrepriseController {

    private final IEntrepriseService enterpriseService;

    @GetMapping("/getAll")
    @Operation(summary = "Get all enterprises", description = "Returns a list of all enterprises")
    public List<Entreprise> getAllEnterprises() {
        return enterpriseService.getAllEnterprises();
    }

    @GetMapping("/getById/{id}")
    @Operation(summary = "Get enterprise by ID", description = "Returns a single enterprise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Entreprise> getEnterpriseById(@PathVariable String id) {
        return enterpriseService.getEnterpriseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @Operation(summary = "Create a new enterprise")
    @ApiResponse(responseCode = "201", description = "Enterprise created")
    public ResponseEntity<Map<String, String>> createEnterprise(@Valid @RequestBody EntrepriseDTO enterpriseDTO) {
        enterpriseService.createEnterprise(enterpriseDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Entreprise added successfully");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    @Operation(summary = "Update an existing enterprise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "404", description = "Enterprise not found")
    })
    public ResponseEntity<Map<String, String>> updateEnterprise(@PathVariable String id,
                                                                @Valid @RequestBody EntrepriseDTO enterpriseDTO) {
        enterpriseService.updateEnterprise(id, enterpriseDTO);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Entreprise updated successfully");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete an enterprise")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Enterprise not found")
    })
    public ResponseEntity<Map<String, String>> deleteEnterprise(@PathVariable String id) {
        enterpriseService.deleteEnterprise(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Entreprise deleted successfully");

        return ResponseEntity.ok(response);
    }
}