package tn.esprit.spring.b2u.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.repository.ProjetRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProjectAiService {

    private final ProjetRepository projetRepository;

    public Map<String, Object> analyzeProject(String projetId) {
        Projet projet = projetRepository.findById(projetId).orElseThrow();
        Map<String, Object> result = new HashMap<>();

        String description = projet.getDescription() != null ?
                projet.getDescription().toLowerCase() : "";

        // 1. Suggérer les compétences
        List<String> suggestedSkills = suggestSkills(description);
        result.put("suggestedSkills", suggestedSkills);

        // 2. Estimer le délai
        String estimatedDelay = estimateDelay(description, 3);        result.put("estimatedDelay", estimatedDelay);

        // 3. Recommander la taille de l'équipe
        int recommendedTeamSize = recommendTeamSize(description);
        result.put("recommendedTeamSize", recommendedTeamSize);

        // 4. Évaluer la difficulté
        String difficulty = evaluateDifficulty(description);
        result.put("difficulty", difficulty);

        // 5. Résumé global
        result.put("summary", generateSummary(projet, suggestedSkills,
                estimatedDelay, difficulty));

        return result;
    }

    private List<String> suggestSkills(String description) {
        List<String> skills = new ArrayList<>();

        if (description.contains("web") || description.contains("frontend") ||
                description.contains("angular") || description.contains("react")) {
            skills.add("Angular");
            skills.add("HTML/CSS");
        }
        if (description.contains("backend") || description.contains("api") ||
                description.contains("spring") || description.contains("serveur")) {
            skills.add("Spring Boot");
            skills.add("REST API");
        }
        if (description.contains("mobile") || description.contains("android") ||
                description.contains("ios")) {
            skills.add("Flutter");
            skills.add("Mobile Development");
        }
        if (description.contains("ia") || description.contains("ai") ||
                description.contains("machine learning") || description.contains("intelligence")) {
            skills.add("Python");
            skills.add("Machine Learning");
        }
        if (description.contains("data") || description.contains("base de données") ||
                description.contains("mongodb")) {
            skills.add("MongoDB");
            skills.add("Database Design");
        }
        if (skills.isEmpty()) {
            skills.add("Java");
            skills.add("Spring Boot");
            skills.add("MongoDB");
        }
        return skills;
    }

    private String estimateDelay(String description, int teamSize) {
        int baseWeeks = 4;

        if (description.contains("complexe") || description.contains("avancé") ||
                description.contains("ia") || description.contains("machine learning")) {
            baseWeeks = 12;
        } else if (description.contains("simple") || description.contains("basique")) {
            baseWeeks = 2;
        } else if (description.length() > 200) {
            baseWeeks = 8;
        }

        if (teamSize > 3) baseWeeks = (int)(baseWeeks * 0.7);

        return baseWeeks + " semaines";
    }

    private int recommendTeamSize(String description) {
        if (description.contains("complexe") || description.contains("avancé") ||
                description.length() > 300) {
            return 5;
        } else if (description.contains("simple") || description.contains("basique")) {
            return 2;
        }
        return 3;
    }

    private String evaluateDifficulty(String description) {
        int score = 0;

        if (description.contains("ia") || description.contains("machine learning")) score += 3;
        if (description.contains("complexe") || description.contains("avancé")) score += 2;
        if (description.contains("mobile") && description.contains("web")) score += 2;
        if (description.contains("temps réel") || description.contains("real-time")) score += 2;
        if (description.contains("simple") || description.contains("basique")) score -= 2;

        if (score >= 4) return "difficile";
        if (score >= 2) return "moyen";
        return "facile";
    }

    private String generateSummary(Projet projet, List<String> skills,
                                   String delay, String difficulty) {
        return String.format(
                "🤖 Analyse IA du projet '%s' :\n" +
                        "📊 Difficulté : %s\n" +
                        "⏱️ Délai estimé : %s\n" +
                        "🛠️ Compétences suggérées : %s\n" +
                        "👥 Taille d'équipe recommandée : %d membres",
                projet.getTitle(),
                difficulty,
                delay,
                String.join(", ", skills),
                recommendTeamSize(projet.getDescription() != null ?
                        projet.getDescription().toLowerCase() : "")
        );
    }
}