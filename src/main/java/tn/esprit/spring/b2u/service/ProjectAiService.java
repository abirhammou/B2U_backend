package tn.esprit.spring.b2u.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.repository.ProjetRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

        List<String> technologies = projet.getAllRequiredSkills();

        // 1. Suggérer les compétences basées sur les technologies
        List<String> suggestedSkills = suggestSkills(description, technologies);
        result.put("suggestedSkills", suggestedSkills);

        // 2. Calculer le délai basé sur la deadline réelle
        long daysUntilDeadline = calculateDaysUntilDeadline(projet);
        String estimatedDelay = formatDelay(daysUntilDeadline);
        result.put("estimatedDelay", estimatedDelay);
        result.put("daysUntilDeadline", daysUntilDeadline);

        // 3. Nombre de sprints recommandé basé sur la deadline
        int recommendedSprints = calculateRecommendedSprints(daysUntilDeadline);
        result.put("recommendedSprints", recommendedSprints);

        // 4. Recommander la taille de l'équipe
        int recommendedTeamSize = recommendTeamSize(description, technologies);
        result.put("recommendedTeamSize", recommendedTeamSize);

        // 5. Score de complexité (1-10)
        int complexityScore = calculateComplexityScore(description, technologies);
        result.put("complexityScore", complexityScore);

        // 6. Évaluer la difficulté
        String difficulty = evaluateDifficulty(complexityScore);
        result.put("difficulty", difficulty);

        // 7. Tâches suggérées basées sur les technologies
        List<String> suggestedTasks = suggestTasks(technologies, description);
        result.put("suggestedTasks", suggestedTasks);

        // 8. Résumé global
        result.put("summary", generateSummary(projet, suggestedSkills,
                estimatedDelay, difficulty, complexityScore,
                recommendedSprints));

        return result;
    }

    private long calculateDaysUntilDeadline(Projet projet) {
        if (projet.getDeadline() == null) return 90; // défaut 3 mois
        LocalDate deadline = projet.getDeadline().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate today = LocalDate.now();
        long days = ChronoUnit.DAYS.between(today, deadline);
        return Math.max(days, 14); // minimum 2 semaines
    }

    private String formatDelay(long days) {
        if (days < 14) return "moins de 2 semaines";
        if (days < 30) return (days / 7) + " semaines";
        if (days < 365) return (days / 30) + " mois";
        return (days / 365) + " an(s)";
    }

    private int calculateRecommendedSprints(long days) {
        // 1 sprint = 2 semaines (14 jours)
        int sprints = (int) Math.ceil(days / 14.0);
        return Math.max(1, Math.min(sprints, 12)); // entre 1 et 12 sprints
    }

    private List<String> suggestSkills(String description, List<String> technologies) {
        Set<String> skills = new LinkedHashSet<>();

        // Basé sur les technologies existantes
        for (String tech : technologies) {
            String t = tech.toLowerCase();
            if (t.contains("angular")) { skills.add("Angular"); skills.add("TypeScript"); }
            if (t.contains("react")) { skills.add("React"); skills.add("JavaScript"); }
            if (t.contains("spring")) { skills.add("Spring Boot"); skills.add("Java"); }
            if (t.contains("mongodb")) { skills.add("MongoDB"); skills.add("NoSQL"); }
            if (t.contains("python")) { skills.add("Python"); }
            if (t.contains("flutter")) { skills.add("Flutter"); skills.add("Dart"); }
        }

        // Basé sur la description
        if (description.contains("ia") || description.contains("machine learning")) {
            skills.add("Python"); skills.add("TensorFlow");
        }
        if (description.contains("mobile")) {
            skills.add("Flutter"); skills.add("React Native");
        }
        if (description.contains("api")) {
            skills.add("REST API"); skills.add("Postman");
        }
        if (description.contains("sécurité") || description.contains("auth")) {
            skills.add("JWT"); skills.add("Spring Security");
        }

        if (skills.isEmpty()) {
            skills.add("Java"); skills.add("Spring Boot"); skills.add("MongoDB");
        }

        return new ArrayList<>(skills);
    }

    private List<String> suggestTasks(List<String> technologies, String description) {
        List<String> tasks = new ArrayList<>();

        // Tâches de base toujours présentes
        tasks.add("Analyse et spécification des besoins");
        tasks.add("Conception de l'architecture");

        // Tâches basées sur les technologies
        for (String tech : technologies) {
            String t = tech.toLowerCase();
            if (t.contains("angular")) tasks.add("Setup et configuration Angular");
            if (t.contains("spring")) tasks.add("Configuration Spring Boot et sécurité");
            if (t.contains("mongodb")) tasks.add("Conception du schéma MongoDB");
            if (t.contains("react")) tasks.add("Setup React et composants UI");
            if (t.contains("flutter")) tasks.add("Setup Flutter et navigation");
            if (t.contains("python")) tasks.add("Setup environnement Python");
        }

        // Tâches basées sur la description
        if (description.contains("auth") || description.contains("login")) {
            tasks.add("Implémentation authentification JWT");
        }
        if (description.contains("dashboard")) {
            tasks.add("Développement du tableau de bord");
        }
        if (description.contains("api")) {
            tasks.add("Développement et documentation des APIs REST");
        }

        // Tâches finales toujours présentes
        tasks.add("Tests unitaires et d'intégration");
        tasks.add("Déploiement et mise en production");

        return tasks;
    }

    private int recommendTeamSize(String description, List<String> technologies) {
        int size = 2; // base
        if (technologies.size() > 3) size++;
        if (description.contains("complexe") || description.contains("avancé")) size++;
        if (description.contains("ia") || description.contains("machine learning")) size++;
        if (description.contains("mobile") && description.contains("web")) size++;
        return Math.min(size, 6);
    }

    private int calculateComplexityScore(String description, List<String> technologies) {
        int score = 3; // base

        // Technologies
        score += Math.min(technologies.size(), 3);

        // Description
        if (description.contains("ia") || description.contains("machine learning")) score += 2;
        if (description.contains("temps réel") || description.contains("real-time")) score += 2;
        if (description.contains("complexe") || description.contains("avancé")) score += 1;
        if (description.contains("mobile") && description.contains("web")) score += 1;
        if (description.contains("simple") || description.contains("basique")) score -= 2;

        return Math.max(1, Math.min(score, 10));
    }

    private String evaluateDifficulty(int complexityScore) {
        if (complexityScore >= 7) return "difficile";
        if (complexityScore >= 4) return "moyen";
        return "facile";
    }

    private String generateSummary(Projet projet, List<String> skills,
                                   String delay, String difficulty,
                                   int complexityScore, int recommendedSprints) {
        return String.format(
                "🤖 Analyse IA du projet '%s'\n\n" +
                        "📊 Difficulté : %s (score %d/10)\n" +
                        "⏱️ Délai estimé : %s\n" +
                        "🏃 Sprints recommandés : %d sprints de 2 semaines\n" +
                        "🛠️ Compétences suggérées : %s\n" +
                        "👥 Taille d'équipe recommandée : %d membres",
                projet.getTitle(),
                difficulty,
                complexityScore,
                delay,
                recommendedSprints,
                String.join(", ", skills),
                recommendTeamSize(
                        projet.getDescription() != null ? projet.getDescription().toLowerCase() : "",
                        projet.getAllRequiredSkills()
                )
        );
    }
}