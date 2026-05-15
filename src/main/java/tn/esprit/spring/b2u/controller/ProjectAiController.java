package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.service.ProjectAiService;

import java.util.Map;

@RestController
@RequestMapping("/api/ai/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectAiController {

    private final ProjectAiService projectAiService;

    @GetMapping("/analyze/{projetId}")
    public Map<String, Object> analyzeProject(@PathVariable String projetId) {
        return projectAiService.analyzeProject(projetId);
    }
}