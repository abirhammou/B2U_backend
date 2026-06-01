package tn.esprit.spring.b2u.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.spring.b2u.entity.StudentProfile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiAssistantService {

    @Value("${groq.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    // ── B: Generate project description + required skills ─────────────────
    public Map<String, Object> generateProjectDescription(String title, String sector) {
        String prompt = """
            Tu es un expert RH et tech. Génère une description de projet professionnelle pour:
            Titre: "%s", Secteur: "%s"

            Réponds UNIQUEMENT en JSON valide avec exactement ces champs:
            {
              "description": "Description complète et professionnelle du projet (150-200 mots)",
              "requiredSkills": ["skill1", "skill2", "skill3", "skill4", "skill5"]
            }
            Aucun texte avant ou après le JSON.
        """.formatted(title, sector);

        String text = callGroq(prompt, 800);
        try {
            text = cleanJson(text);
            JsonNode json = objectMapper.readTree(text);
            Map<String, Object> result = new HashMap<>();
            result.put("description", json.path("description").asText());
            List<String> skills = new ArrayList<>();
            json.path("requiredSkills").forEach(n -> skills.add(n.asText()));
            result.put("requiredSkills", skills);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing réponse Groq: " + e.getMessage());
        }
    }

    // ── C: Skill gap analysis ─────────────────────────────────────────────
    public Map<String, Object> analyzeSkillGap(List<String> studentSkills,
                                                List<String> projectSkills,
                                                String projectTitle) {
        String prompt = """
            Tu es un conseiller carrière. Analyse l'écart de compétences pour ce projet.

            Projet: "%s"
            Compétences requises: %s
            Compétences de l'étudiant: %s

            Réponds UNIQUEMENT en JSON valide:
            {
              "missingSkills": ["skill1", "skill2"],
              "matchedSkills": ["skill3", "skill4"],
              "advice": "Conseil concret et bienveillant sur comment acquérir les compétences manquantes (2-3 phrases)",
              "matchPercent": 65
            }
            Aucun texte avant ou après le JSON.
        """.formatted(projectTitle, projectSkills.toString(), studentSkills.toString());

        String text = callGroq(prompt, 600);
        try {
            text = cleanJson(text);
            JsonNode json = objectMapper.readTree(text);
            Map<String, Object> result = new HashMap<>();
            List<String> missing = new ArrayList<>();
            json.path("missingSkills").forEach(n -> missing.add(n.asText()));
            result.put("missingSkills", missing);
            List<String> matched = new ArrayList<>();
            json.path("matchedSkills").forEach(n -> matched.add(n.asText()));
            result.put("matchedSkills", matched);
            result.put("advice", json.path("advice").asText());
            result.put("matchPercent", json.path("matchPercent").asInt());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing réponse Groq: " + e.getMessage());
        }
    }

    // ── D: Candidature feedback for refused candidates ────────────────────
    public String generateCandidatureFeedback(String candidateName, String projectTitle,
                                               int score, String matchingDetails, String specialite) {
        String prompt = """
            Tu es un recruteur bienveillant. Rédige un message de feedback personnalisé pour un candidat dont la candidature n'a pas été retenue.

            Candidat: %s
            Projet: %s
            Score de matching: %d%%
            Spécialité: %s
            Détails du matching: %s

            Rédige un feedback:
            - En français
            - Bienveillant et constructif
            - Mentionner le score et pourquoi le profil ne correspond pas entièrement
            - Donner 2-3 conseils concrets pour améliorer le profil
            - 150-200 mots maximum
            - Sans titre, juste le texte du message
        """.formatted(candidateName, projectTitle, score, specialite, matchingDetails != null ? matchingDetails : "non disponible");

        return callGroq(prompt, 500);
    }

    // ── Shared Groq call ──────────────────────────────────────────────────
    private String callGroq(String prompt, int maxTokens) {
        Map<String, Object> body = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(Map.of("role", "user", "content", prompt)),
            "max_tokens", maxTokens
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.groq.com/openai/v1/chat/completions",
            HttpMethod.POST, request, String.class
        );
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lecture réponse Groq: " + e.getMessage());
        }
    }

    private String cleanJson(String text) {
        return text.replaceAll("```json", "").replaceAll("```", "").trim();
    }

    // ── E: Evaluate student profile ───────────────────────────────────────
    public Map<String, Object> evaluateProfile(StudentProfile profile) {
        String prompt = """
            Tu es un expert en évaluation de carrière. Analyse le profil de cet étudiant et génère un score AI et une évaluation détaillée.

            Profil:
            Bio: %s
            Compétences techniques: %s
            Compétences soft: %s
            Éducation: %s
            Expérience: %s

            Réponds UNIQUEMENT en JSON valide:
            {
              "aiScore": 85,
              "detailedEvaluation": "Une évaluation complète de 3-4 paragraphes en français...",
              "categoryScores": {
                "Technical Skills": 80,
                "Soft Skills": 90,
                "Education": 75,
                "Experience": 85
              }
            }
            Aucun texte avant ou après le JSON.
            """.formatted(
                profile.getBio(),
                profile.getTechnicalSkills(),
                profile.getSoftSkills(),
                profile.getEducation(),
                profile.getWorkExperience()
        );

        String text = callGroq(prompt, 1000);
        try {
            text = cleanJson(text);
            JsonNode json = objectMapper.readTree(text);
            Map<String, Object> result = new HashMap<>();
            result.put("aiScore", json.path("aiScore").asInt());
            result.put("detailedEvaluation", json.path("detailedEvaluation").asText());
            Map<String, Integer> catScores = new HashMap<>();
            JsonNode catNode = json.path("categoryScores");
            catNode.fieldNames().forEachRemaining(name -> catScores.put(name, catNode.get(name).asInt()));
            result.put("categoryScores", catScores);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing réponse Groq: " + e.getMessage());
        }
    }
}
