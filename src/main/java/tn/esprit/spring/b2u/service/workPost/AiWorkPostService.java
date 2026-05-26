package tn.esprit.spring.b2u.service.workPost;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.WorkPostStatus;
import tn.esprit.spring.b2u.entity.WorkMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiWorkPostService {

    @Value("${groq.api.key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public WorkPost generateWorkPost(String title, String sector) {

        String prompt = """
            Tu es un expert RH. Génère une fiche de poste professionnelle pour le titre "%s" dans le secteur "%s".
            Réponds UNIQUEMENT en JSON valide avec exactement ces champs :
            {
              "title": "...",
              "requiredSkills": "...",
              "hoursPerWeek": <nombre entier>,
              "workMode": "REMOTE" ou "ONSITE" ou "HYBRID"
            }
            Aucun texte avant ou après le JSON.
        """.formatted(title, sector);

        Map<String, Object> body = Map.of(
                "model", "llama-3.3-70b-versatile",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 1000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.groq.com/openai/v1/chat/completions",
                HttpMethod.POST,
                request,
                String.class
        );

        return parseResponse(response.getBody());
    }

    private WorkPost parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Groq uses OpenAI-compatible response format
            String text = root
                    .path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            // Clean markdown backticks if present
            text = text.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode json = objectMapper.readTree(text);

            WorkPost post = new WorkPost();
            post.setTitle(json.path("title").asText());
            post.setRequiredSkills(json.path("requiredSkills").asText());
            post.setHoursPerWeek(json.path("hoursPerWeek").asInt());
            post.setStatus(WorkPostStatus.ACTIVE);
            post.setCreatedAt(LocalDateTime.now());

            String workModeStr = json.path("workMode").asText("ONSITE");
            try {
                post.setWorkMode(WorkMode.valueOf(workModeStr));
            } catch (IllegalArgumentException e) {
                post.setWorkMode(WorkMode.ONSITE);
            }

            return post;

        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing réponse Groq: " + e.getMessage());
        }
    }
}