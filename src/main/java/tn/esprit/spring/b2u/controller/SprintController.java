package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.Sprint;
import tn.esprit.spring.b2u.service.sprint.SprintService;

import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SprintController {

    private final SprintService sprintService;

    @GetMapping
    public List<Sprint> getAllSprints() {
        return sprintService.getAllSprints();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> getSprintById(@PathVariable String id) {
        return sprintService.getSprintById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projet/{projetId}")
    public List<Sprint> getSprintsByProjet(@PathVariable String projetId) {
        return sprintService.getSprintsByProjet(projetId);
    }

    @PostMapping
    public Sprint createSprint(@RequestBody Sprint sprint) {
        return sprintService.createSprint(sprint);
    }

    @PutMapping("/{id}")
    public Sprint updateSprint(@PathVariable String id, @RequestBody Sprint sprint) {
        return sprintService.updateSprint(id, sprint);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable String id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate/{projetId}")
    public List<Sprint> generateSprints(@PathVariable String projetId) {
        return sprintService.generateSprintsForProjet(projetId);
    }
}