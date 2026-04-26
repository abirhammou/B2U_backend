package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.service.projet.ProjetService;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjetController {

    private final ProjetService projetService;

    // GET tous les projets
    @GetMapping
    public List<Projet> getAllProjets() {
        return projetService.getAllProjets();
    }

    // GET un projet par ID
    @GetMapping("/{id}")
    public ResponseEntity<Projet> getProjetById(@PathVariable String id) {
        return projetService.getProjetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST créer un projet
    @PostMapping
    public Projet createProjet(@RequestBody Projet projet) {
        return projetService.createProjet(projet);
    }

    // PUT modifier un projet
    @PutMapping("/{id}")
    public Projet updateProjet(@PathVariable String id, @RequestBody Projet projet) {
        return projetService.updateProjet(id, projet);
    }

    // DELETE supprimer un projet
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjet(@PathVariable String id) {
        projetService.deleteProjet(id);
        return ResponseEntity.noContent().build();
    }
}