package tn.esprit.spring.b2u.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Candidature;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InterviewQuestionAiService {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hf.generation.api.url:}")
    private String generationUrl;

    @Value("${hf.api.token:}")
    private String huggingFaceToken;

    public String generateInterviewPreparation(Candidature candidature, List<String> missingSkills) {
        if (!isConfigured()) {
            return "";
        }

        try {
            String prompt = buildPrompt(candidature, missingSkills);
            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", prompt);
            payload.put("parameters", Map.of(
                    "max_new_tokens", 650,
                    "temperature", 0.7,
                    "return_full_text", false
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(generationUrl))
                    .timeout(Duration.ofSeconds(75))
                    .header("Authorization", "Bearer " + huggingFaceToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("Hugging Face interview generation error: HTTP " + response.statusCode());
                return "";
            }

            return cleanGeneratedText(extractGeneratedText(response.body()));
        } catch (Exception e) {
            System.out.println("Hugging Face interview generation unavailable: " + e.getMessage());
            return "";
        }
    }

    private boolean isConfigured() {
        return generationUrl != null && !generationUrl.isBlank()
                && huggingFaceToken != null && !huggingFaceToken.isBlank();
    }

    private String buildPrompt(Candidature candidature, List<String> missingSkills) {
        String projectTitle = valueOrDefault(candidature.getProjectTitle(), "le projet");
        String projectType = valueOrDefault(candidature.getProjectType(), "PROJET");
        String skills = candidature.getCompetences() == null || candidature.getCompetences().isEmpty()
                ? "non precisees"
                : String.join(", ", candidature.getCompetences());
        String missing = missingSkills == null || missingSkills.isEmpty()
                ? "aucune competence manquante critique"
                : String.join(", ", missingSkills);

        return """
                Tu es un recruteur technique.
                Genere une preparation d'entretien en francais simple pour un etudiant.
                Le format doit etre exactement:
                Preparation AI a l'entretien - <nom projet>
                Questions techniques:
                1. <question>
                Reponse modele: <reponse courte>
                2. <question>
                Reponse modele: <reponse courte>
                3. <question>
                Reponse modele: <reponse courte>
                Questions sur les points a renforcer:
                4. <question>
                Reponse modele: <reponse courte>
                5. <question>
                Reponse modele: <reponse courte>
                Questions comportementales:
                6. <question>
                Reponse modele: <reponse courte>
                7. <question>
                Reponse modele: <reponse courte>
                Conseils AI:
                - <conseil>
                - <conseil>

                Contexte:
                Projet: %s
                Type: %s
                Experience candidat: %d an(s)
                Competences CV: %s
                Competences a renforcer: %s
                Recommendation matching: %s
                """.formatted(
                projectTitle,
                projectType,
                candidature.getAnneeExperience(),
                skills,
                missing,
                valueOrDefault(candidature.getRecommendation(), "non disponible")
        );
    }

    private String extractGeneratedText(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (root.isArray() && root.size() > 0) {
            JsonNode first = root.get(0);
            if (first.has("generated_text")) {
                return first.get("generated_text").asText();
            }
            if (first.has("summary_text")) {
                return first.get("summary_text").asText();
            }
        }
        if (root.isObject() && root.has("generated_text")) {
            return root.get("generated_text").asText();
        }
        return "";
    }

    private String cleanGeneratedText(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.trim();
        return cleaned.contains("Reponse modele") ? cleaned : "";
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
