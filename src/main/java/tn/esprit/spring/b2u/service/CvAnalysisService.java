package tn.esprit.spring.b2u.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CvAnalysisService {

    private static final int MAX_TEXT_CHARS_FOR_HF = 4000;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${hf.api.url:}")
    private String huggingFaceUrl;

    @Value("${hf.api.token:}")
    private String huggingFaceToken;

    @Value("${affinda.api.url:}")
    private String affindaUrl;

    @Value("${affinda.api.token:}")
    private String affindaToken;

    @Value("${affinda.workspace:}")
    private String affindaWorkspace;

    @Value("${affinda.collection:}")
    private String affindaCollection;

    public List<String> extractSkillsFromBytes(byte[] pdfBytes) {
        System.out.println("========== CV ANALYSIS START ==========");

        try {
            String extractedText = extractTextFromBytes(pdfBytes);
            Set<String> skills = new LinkedHashSet<>();

            skills.addAll(extractSkillsWithAffinda(pdfBytes));
            skills.addAll(extractSkillsWithHuggingFace(extractedText));
            skills.addAll(extractSkillsFromText(extractedText));

            List<String> result = normalizeAndLimit(skills);
            System.out.println("CV skills extracted: " + result);
            System.out.println("========== CV ANALYSIS END ==========");

            return result.isEmpty() ? getDefaultSoftwareEngineeringSkills() : result;

        } catch (Exception e) {
            System.out.println("CV analysis failed: " + e.getMessage());
            return getDefaultSkills();
        }
    }

    private List<String> extractSkillsWithAffinda(byte[] pdfBytes) {
        if (!isConfigured(affindaUrl, affindaToken)) {
            System.out.println("Affinda API skipped: missing AFFINDA_API_TOKEN.");
            return List.of();
        }

        try {
            String boundary = "----B2UAffindaBoundary" + System.currentTimeMillis();
            byte[] body = buildAffindaMultipartBody(pdfBytes, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(affindaUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + affindaToken)
                    .header("Accept", "application/json")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("Affinda API error: HTTP " + response.statusCode());
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.body());
            Set<String> skills = new LinkedHashSet<>();
            collectAffindaSkills(root, skills);
            System.out.println("Affinda skills: " + skills);
            return new ArrayList<>(skills);

        } catch (Exception e) {
            System.out.println("Affinda API unavailable: " + e.getMessage());
            return List.of();
        }
    }

    private byte[] buildAffindaMultipartBody(byte[] pdfBytes, String boundary) throws IOException {
        ByteArrayBuilder builder = new ByteArrayBuilder();
        addMultipartField(builder, boundary, "wait", "true");
        addMultipartField(builder, boundary, "compact", "true");

        if (affindaWorkspace != null && !affindaWorkspace.isBlank()) {
            addMultipartField(builder, boundary, "workspace", affindaWorkspace);
        }
        if (affindaCollection != null && !affindaCollection.isBlank()) {
            addMultipartField(builder, boundary, "collection", affindaCollection);
        }

        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"cv.pdf\"\r\n");
        builder.append("Content-Type: application/pdf\r\n\r\n");
        builder.append(pdfBytes);
        builder.append("\r\n--").append(boundary).append("--\r\n");
        return builder.toByteArray();
    }

    private void addMultipartField(ByteArrayBuilder builder, String boundary, String name, String value) {
        builder.append("--").append(boundary).append("\r\n");
        builder.append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n");
        builder.append(value).append("\r\n");
    }

    private void collectAffindaSkills(JsonNode node, Set<String> skills) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String name = field.getKey().toLowerCase();
                JsonNode value = field.getValue();

                if (name.contains("skill") || name.contains("competenc")) {
                    collectTextValues(value, skills);
                }
                collectAffindaSkills(value, skills);
            }
            return;
        }

        if (node.isArray()) {
            node.forEach(child -> collectAffindaSkills(child, skills));
        }
    }

    private void collectTextValues(JsonNode node, Set<String> values) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isTextual()) {
            addIfSkillLike(values, node.asText());
            return;
        }

        if (node.isObject()) {
            List<String> preferredKeys = Arrays.asList("name", "raw", "parsed", "value", "text");
            for (String key : preferredKeys) {
                JsonNode value = node.get(key);
                if (value != null && value.isTextual()) {
                    addIfSkillLike(values, value.asText());
                }
            }
            node.fields().forEachRemaining(field -> collectTextValues(field.getValue(), values));
            return;
        }

        if (node.isArray()) {
            node.forEach(child -> collectTextValues(child, values));
        }
    }

    private List<String> extractSkillsWithHuggingFace(String text) {
        if (!isConfigured(huggingFaceUrl, huggingFaceToken) || text == null || text.isBlank()) {
            System.out.println("Hugging Face API skipped: missing HF_API_TOKEN or empty CV text.");
            return List.of();
        }

        try {
            String safeText = text.length() > MAX_TEXT_CHARS_FOR_HF
                    ? text.substring(0, MAX_TEXT_CHARS_FOR_HF)
                    : text;

            Map<String, Object> payload = new HashMap<>();
            payload.put("inputs", safeText);
            payload.put("parameters", Map.of("aggregation_strategy", "simple"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(huggingFaceUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + huggingFaceToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.out.println("Hugging Face API error: HTTP " + response.statusCode());
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.body());
            Set<String> skills = new LinkedHashSet<>();
            collectHuggingFaceTerms(root, skills);
            System.out.println("Hugging Face terms: " + skills);
            return new ArrayList<>(skills);

        } catch (Exception e) {
            System.out.println("Hugging Face API unavailable: " + e.getMessage());
            return List.of();
        }
    }

    private void collectHuggingFaceTerms(JsonNode node, Set<String> skills) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isArray()) {
            node.forEach(child -> collectHuggingFaceTerms(child, skills));
            return;
        }

        if (node.isObject()) {
            JsonNode word = node.get("word");
            if (word == null) {
                word = node.get("entity");
            }
            if (word != null && word.isTextual()) {
                String candidate = word.asText().replace("##", "").trim();
                if (isKnownTechnology(candidate)) {
                    addIfSkillLike(skills, candidate);
                }
            }
            node.fields().forEachRemaining(field -> collectHuggingFaceTerms(field.getValue(), skills));
        }
    }

    private String extractTextFromBytes(byte[] pdfBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new IOException("Failed to extract PDF text", e);
        }
    }

    private List<String> extractSkillsFromText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        Set<String> foundSkills = new HashSet<>();
        String lowerText = text.toLowerCase();

        for (String skill : getKnownSkills()) {
            String lowerSkill = skill.toLowerCase();
            if (lowerText.contains(lowerSkill)) {
                foundSkills.add(capitalizeSkill(skill));
            }
        }

        if (foundSkills.isEmpty()) {
            extractSkillsFromSkillsSection(lowerText, foundSkills);
        }

        return normalizeAndLimit(foundSkills);
    }

    private void extractSkillsFromSkillsSection(String lowerText, Set<String> foundSkills) {
        String[] sectionMarkers = {
                "competences", "skills", "technical skills",
                "technologies", "outils", "tools", "langages", "frameworks"
        };

        for (String marker : sectionMarkers) {
            int index = lowerText.indexOf(marker);
            if (index != -1) {
                int start = index + marker.length();
                int end = Math.min(start + 1000, lowerText.length());
                String section = lowerText.substring(start, end);

                int nextMarker = findNextMarker(section, sectionMarkers);
                if (nextMarker != -1) {
                    section = section.substring(0, nextMarker);
                }

                String[] possibleSkills = section.split("[,;\\n\\r]");
                for (String possible : possibleSkills) {
                    addIfSkillLike(foundSkills, possible);
                }
                break;
            }
        }
    }

    private int findNextMarker(String text, String[] markers) {
        int minIndex = Integer.MAX_VALUE;
        for (String marker : markers) {
            int index = text.indexOf(marker);
            if (index != -1 && index < minIndex) {
                minIndex = index;
            }
        }
        return minIndex != Integer.MAX_VALUE ? minIndex : -1;
    }

    private void addIfSkillLike(Set<String> skills, String value) {
        if (value == null) {
            return;
        }
        String cleaned = value
                .replaceAll("[\\[\\]{}()\"']", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.length() >= 2 && cleaned.length() <= 50 && !cleaned.contains("http") && !cleaned.contains("@")) {
            skills.add(capitalizeSkill(cleaned));
        }
    }

    private boolean isKnownTechnology(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            return false;
        }
        String lower = candidate.toLowerCase().trim();
        return getKnownSkills().stream().anyMatch(skill -> skill.equalsIgnoreCase(lower));
    }

    private List<String> normalizeAndLimit(Set<String> skills) {
        List<String> result = new ArrayList<>(skills);
        result.removeIf(skill -> skill == null || skill.isBlank());
        Collections.sort(result);
        if (result.size() > 20) {
            return result.subList(0, 20);
        }
        return result;
    }

    private boolean isConfigured(String url, String token) {
        return url != null && !url.isBlank() && token != null && !token.isBlank();
    }

    private List<String> getKnownSkills() {
        return Arrays.asList(
                "java", "python", "javascript", "typescript", "c++", "c#", "php", "ruby",
                "go", "swift", "kotlin", "rust", "scala", "spring", "spring boot",
                "spring mvc", "spring security", "hibernate", "jpa", "node.js", "express",
                "django", "flask", "fastapi", "laravel", "symfony", "react", "react js",
                "next.js", "angular", "angular js", "vue", "vue.js", "svelte", "bootstrap",
                "tailwind css", "material ui", "mysql", "postgresql", "mongodb", "oracle",
                "sql server", "redis", "firebase", "aws", "azure", "gcp", "docker",
                "kubernetes", "jenkins", "gitlab ci", "github actions", "terraform",
                "ansible", "microservices", "clean architecture", "tdd", "bdd", "solid",
                "design patterns", "junit", "mockito", "selenium", "cypress", "playwright",
                "jest", "postman", "rest", "rest api", "graphql", "soap", "grpc",
                "git", "github", "gitlab", "scrum", "kanban", "maven", "gradle",
                "npm", "yarn", "arduino", "raspberry pi", "esp32", "mqtt", "iot",
                "embedded systems", "freertos", "rtos"
        );
    }

    private String capitalizeSkill(String skill) {
        if (skill == null || skill.isBlank()) return skill;

        String lowerSkill = skill.toLowerCase().trim();
        Map<String, String> specialCases = new HashMap<>();
        specialCases.put("c++", "C++");
        specialCases.put("c#", "C#");
        specialCases.put("freertos", "FreeRTOS");
        specialCases.put("mqtt", "MQTT");
        specialCases.put("esp32", "ESP32");
        specialCases.put("aws", "AWS");
        specialCases.put("gcp", "GCP");
        specialCases.put("iot", "IoT");
        specialCases.put("rtos", "RTOS");
        specialCases.put("node.js", "Node.js");
        specialCases.put("react js", "React.js");
        specialCases.put("vue.js", "Vue.js");
        specialCases.put("spring boot", "Spring Boot");
        specialCases.put("rest api", "REST API");

        if (specialCases.containsKey(lowerSkill)) {
            return specialCases.get(lowerSkill);
        }

        String[] words = lowerSkill.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isBlank()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private List<String> getDefaultSkills() {
        return Arrays.asList("Java", "Spring Boot", "Communication", "Problem Solving");
    }

    private List<String> getDefaultSoftwareEngineeringSkills() {
        return Arrays.asList(
                "Java", "Spring Boot", "Microservices", "REST API", "Git",
                "MongoDB", "Docker", "Agile", "JUnit", "Design Patterns"
        );
    }

    private static class ByteArrayBuilder {
        private byte[] buffer = new byte[0];

        ByteArrayBuilder append(String value) {
            return append(value.getBytes(StandardCharsets.UTF_8));
        }

        ByteArrayBuilder append(byte[] bytes) {
            byte[] next = new byte[buffer.length + bytes.length];
            System.arraycopy(buffer, 0, next, 0, buffer.length);
            System.arraycopy(bytes, 0, next, buffer.length, bytes.length);
            buffer = next;
            return this;
        }

        byte[] toByteArray() {
            return buffer;
        }
    }
}
