package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.spring.b2u.DTO.EquipeDTO;
import tn.esprit.spring.b2u.entity.Equipe;
import tn.esprit.spring.b2u.entity.Task;
import tn.esprit.spring.b2u.service.equipe.IEquipeService;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/equipe")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Gestion des équipes", description = "API pour la gestion des équipes (CRUD)")
public class EquipeController {

    private final IEquipeService equipeService;

    @Value("${openrouter.api.key:}")
    private String OPENROUTER_KEY;

    private static final String OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

    // ───────────────────────── CRUD ─────────────────────────

    @PostMapping("/add")
    public ResponseEntity<?> ajouterEquipe(@Valid @RequestBody EquipeDTO dto) {
        try {
            Equipe equipe = equipeService.ajouterEquipeFromDTO(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(equipe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create equipe: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllEquipes() {
        try {
            List<Equipe> equipes = equipeService.getAllEquipes();
            return ResponseEntity.ok(equipes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch equipes: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipeById(
            @Parameter(description = "ID de l'équipe", required = true)
            @PathVariable String id) {
        try {
            Equipe equipe = equipeService.getEquipeById(id);
            if (equipe == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Équipe introuvable avec l'id: " + id));
            }
            return ResponseEntity.ok(equipe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch equipe: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> modifierEquipe(
            @Valid @RequestBody Equipe equipe,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage,
                            (existing, replacement) -> existing
                    ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", HttpStatus.BAD_REQUEST.value(),
                    "error", "Validation échouée",
                    "errors", errors
            ));
        }

        try {
            Equipe updatedEquipe = equipeService.modifierEquipe(equipe);
            return ResponseEntity.ok(updatedEquipe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update equipe: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> supprimerEquipe(
            @Parameter(description = "ID de l'équipe", required = true)
            @PathVariable String id) {
        try {
            Equipe equipe = equipeService.getEquipeById(id);
            if (equipe == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Équipe introuvable avec l'id: " + id));
            }
            equipeService.supprimerEquipe(id);
            return ResponseEntity.ok(Map.of("message", "Équipe supprimée avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete equipe: " + e.getMessage()));
        }
    }

    // ───────────────────────── GESTION DES TÂCHES ─────────────────────────

    /**
     * Créer une nouvelle tâche (réservé aux entreprises)
     * POST /equipe/{equipeId}/tasks/add
     */
    @PostMapping("/{equipeId}/tasks/add")
    @Operation(summary = "Créer une nouvelle tâche (entreprise)")
    public ResponseEntity<?> addTask(
            @PathVariable String equipeId,
            @RequestBody Map<String, String> request) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);

            Task newTask = new Task();
            newTask.setTitle(request.get("title"));
            newTask.setStatus("TODO");
            newTask.setDescription(request.get("description"));
            newTask.setAssignedTo(request.get("assignedTo"));

            List<Task> tasks = equipe.getTasks();
            if (tasks == null) {
                tasks = new ArrayList<>();
            }
            tasks.add(newTask);
            equipe.setTasks(tasks);
            equipeService.modifierEquipe(equipe);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Tâche créée avec succès", "task", newTask));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Assigner une tâche à un étudiant (réservé aux entreprises)
     * PUT /equipe/{equipeId}/tasks/{taskIndex}/assign
     */
    @PutMapping("/{equipeId}/tasks/{taskIndex}/assign")
    @Operation(summary = "Assigner une tâche à un étudiant (entreprise)")
    public ResponseEntity<?> assignTask(
            @PathVariable String equipeId,
            @PathVariable int taskIndex,
            @RequestBody Map<String, String> request) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);
            String studentId = request.get("studentId");

            if (studentId == null || studentId.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "studentId est requis"));
            }

            List<Task> tasks = equipe.getTasks();
            if (tasks == null || taskIndex < 0 || taskIndex >= tasks.size()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Index de tâche invalide"));
            }

            tasks.get(taskIndex).setAssignedTo(studentId);
            equipeService.modifierEquipe(equipe);

            return ResponseEntity.ok(Map.of(
                    "message", "Tâche assignée avec succès",
                    "studentId", studentId,
                    "taskIndex", taskIndex
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Supprimer une tâche (réservé aux entreprises)
     * DELETE /equipe/{equipeId}/tasks/{taskIndex}
     */
    @DeleteMapping("/{equipeId}/tasks/{taskIndex}")
    @Operation(summary = "Supprimer une tâche (entreprise)")
    public ResponseEntity<?> deleteTask(
            @PathVariable String equipeId,
            @PathVariable int taskIndex) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);
            List<Task> tasks = equipe.getTasks();

            if (tasks == null || taskIndex < 0 || taskIndex >= tasks.size()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Index de tâche invalide"));
            }

            Task removedTask = tasks.remove(taskIndex);
            equipe.setTasks(tasks);
            equipeService.modifierEquipe(equipe);

            return ResponseEntity.ok(Map.of(
                    "message", "Tâche supprimée avec succès",
                    "removedTask", removedTask
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mettre à jour le statut d'une tâche (étudiant)
     * PUT /equipe/{equipeId}/tasks/{taskIndex}/status
     */
    @PutMapping("/{equipeId}/tasks/{taskIndex}/status")
    @Operation(summary = "Mettre à jour le statut d'une tâche (étudiant)")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable String equipeId,
            @PathVariable int taskIndex,
            @RequestBody Map<String, String> body) {
        try {
            String newStatus = body.get("status");
            if (newStatus == null || newStatus.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
            if (!List.of("TODO", "IN_PROGRESS", "DONE").contains(newStatus)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status. Allowed: TODO, IN_PROGRESS, DONE"));
            }

            Equipe equipe = equipeService.getEquipeById(equipeId);
            List<Task> tasks = equipe.getTasks();

            if (tasks == null || taskIndex < 0 || taskIndex >= tasks.size()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Task not found at index: " + taskIndex));
            }

            tasks.get(taskIndex).setStatus(newStatus);
            equipeService.modifierEquipe(equipe);

            return ResponseEntity.ok(Map.of(
                    "message",    "Task status updated successfully",
                    "taskIndex",  taskIndex,
                    "newStatus",  newStatus
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupérer les tâches assignées à un étudiant
     * GET /equipe/{equipeId}/tasks/assigned/{studentId}
     */
    @GetMapping("/{equipeId}/tasks/assigned/{studentId}")
    @Operation(summary = "Tâches assignées à un étudiant")
    public ResponseEntity<?> getTasksByStudent(
            @PathVariable String equipeId,
            @PathVariable String studentId) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);
            List<Task> myTasks = equipe.getTasks() == null
                    ? List.of()
                    : equipe.getTasks().stream()
                    .filter(t -> studentId.equals(t.getAssignedTo()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                    "tasks", myTasks,
                    "count", myTasks.size(),
                    "studentId", studentId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Statistiques des tâches d'une équipe
     * GET /equipe/{equipeId}/tasks/stats
     */
    @GetMapping("/{equipeId}/tasks/stats")
    @Operation(summary = "Statistiques des tâches d'une équipe")
    public ResponseEntity<?> getTaskStats(@PathVariable String equipeId) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);
            List<Task> tasks = equipe.getTasks() == null ? List.of() : equipe.getTasks();

            long todo       = tasks.stream().filter(t -> "TODO".equals(t.getStatus())).count();
            long inProgress = tasks.stream().filter(t -> "IN_PROGRESS".equals(t.getStatus())).count();
            long done       = tasks.stream().filter(t -> "DONE".equals(t.getStatus())).count();
            int  total      = tasks.size();
            double rate     = total > 0 ? (done * 100.0 / total) : 0;

            return ResponseEntity.ok(Map.of(
                    "total",          total,
                    "todo",           todo,
                    "inProgress",     inProgress,
                    "done",           done,
                    "completionRate", Math.round(rate)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ───────────────────────── IA CHAT ─────────────────────────

    @Operation(summary = "Chat IA pour suggestions d'équipes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Réponse IA générée avec succès"),
            @ApiResponse(responseCode = "401", description = "Clé API manquante ou invalide"),
            @ApiResponse(responseCode = "500", description = "Erreur lors de l'appel à l'API IA")
    })
    @PostMapping("/ai/chat")
    public ResponseEntity<?> aiChat(@RequestBody Map<String, Object> body) {

        if (OPENROUTER_KEY == null || OPENROUTER_KEY.trim().isEmpty()) {
            return getMockResponse(body);
        }

        try {
            if (body == null || !body.containsKey("messages")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Missing 'messages' field"));
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");

            if (messages == null || messages.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "'messages' cannot be empty"));
            }

            String userText = "";
            for (int i = messages.size() - 1; i >= 0; i--) {
                Map<String, Object> msg = messages.get(i);
                if ("user".equals(msg.get("role")) && msg.containsKey("content")) {
                    userText = msg.get("content").toString();
                    break;
                }
            }

            if (userText.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "User message content is empty"));
            }

            String system = body.getOrDefault("system",
                            "Tu es un assistant expert en formation d'équipes. Tu aides les étudiants à créer des équipes équilibrées selon leurs compétences. Réponds de manière claire, structurée et bienveillante en français.")
                    .toString();

            Map<String, Object> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", system);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userText);

            Map<String, Object> openRouterBody = new HashMap<>();
            openRouterBody.put("model", "openai/gpt-3.5-turbo");
            openRouterBody.put("messages", List.of(systemMsg, userMsg));
            openRouterBody.put("temperature", 0.7);
            openRouterBody.put("max_tokens", 1000);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(OPENROUTER_KEY);
            headers.set("HTTP-Referer", "http://localhost:4200");
            headers.set("X-Title", "B2U Platform");

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(openRouterBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENROUTER_URL, request, Map.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from OpenRouter API");
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No choices in OpenRouter response");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String text = message.get("content").toString();

            Map<String, Object> result = new HashMap<>();
            result.put("content", List.of(Map.of("text", text)));
            return ResponseEntity.ok(result);

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return getMockResponse(body);
            }
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "API Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get AI response: " + e.getMessage()));
        }
    }

    // ───────────────────────── MOCK RESPONSE ─────────────────────────

    private ResponseEntity<?> getMockResponse(Map<String, Object> body) {
        try {
            String userMessage = "";
            if (body != null && body.containsKey("messages")) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> messages = (List<Map<String, Object>>) body.get("messages");
                if (messages != null && !messages.isEmpty()) {
                    for (int i = messages.size() - 1; i >= 0; i--) {
                        Map<String, Object> msg = messages.get(i);
                        if ("user".equals(msg.get("role")) && msg.containsKey("content")) {
                            userMessage = msg.get("content").toString();
                            break;
                        }
                    }
                }
            }

            String response = generateSmartResponse(userMessage);

            if (OPENROUTER_KEY == null || OPENROUTER_KEY.trim().isEmpty()) {
                response += "\n\n---\n⚠️ **Mode démo** : Pour activer l'IA avancée, ajoutez une clé API OpenRouter dans application.properties";
            }

            Map<String, Object> result = new HashMap<>();
            result.put("content", List.of(Map.of("text", response)));
            return ResponseEntity.ok(result);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getResponseBodyAsString()));
        }
    }

    // ───────────────────────── SMART RESPONSE GENERATOR ─────────────────────────

    private String generateSmartResponse(String userMessage) {
        String m = userMessage.toLowerCase();

        if (m.matches(".*(bonjour|salut|hello|bonsoir|coucou).*")) {
            return "👋 **Bonjour !** Je suis l'assistant B2U.\n\n" +
                    "Je peux vous aider à :\n" +
                    "• Former des équipes équilibrées\n" +
                    "• Évaluer les compétences\n" +
                    "• Gérer les conflits\n" +
                    "• Planifier les projets\n\n" +
                    "Comment puis-je vous aider aujourd'hui ?";
        }

        if (m.matches(".*(disponible|liste|lister|voir|afficher|quelles equipe|quelles équipe|toutes les equipe|toutes les équipe|existent|existantes).*")) {
            return "📋 **Équipes disponibles**\n\n" +
                    "Pour voir les équipes existantes, consultez la liste à gauche de la page.\n\n" +
                    "Vous pouvez :\n" +
                    "• 🔍 **Rechercher** une équipe par nom\n" +
                    "• 🤝 **Rejoindre** une équipe ouverte\n" +
                    "• ➕ **Créer** votre propre équipe\n\n" +
                    "Besoin d'aide pour rejoindre ou créer une équipe ? 🙋";
        }

        if (m.matches(".*(rejoindre|intégrer|integrer|candidater|postuler|demande adhesion|adhésion).*")) {
            return "🚀 **Comment rejoindre une équipe ?**\n\n" +
                    "1. Parcourez la liste des équipes disponibles\n" +
                    "2. Consultez les besoins de chaque équipe\n" +
                    "3. Cliquez sur **Rejoindre** pour envoyer une demande\n" +
                    "4. Attendez la validation du responsable\n\n" +
                    "💡 Mettez à jour votre profil de compétences pour augmenter vos chances !";
        }

        if (m.matches(".*(former|créer|creer|constituer|assembler|monter|nouvelle equipe|nouvelle équipe|new team).*")) {
            return "💡 **Comment former une équipe équilibrée ?**\n\n" +
                    "1. **Identifiez les compétences nécessaires** : Listez les technologies requises\n" +
                    "2. **Évaluez les profils** : Notez les compétences (1-5)\n" +
                    "3. **Assurez la complémentarité** : Mélangez experts et débutants\n" +
                    "4. **Équilibre des rôles** : Leaders, créatifs, analystes, exécutants\n" +
                    "5. **Taille idéale** : 3 à 5 personnes par équipe\n\n" +
                    "✨ Utilisez notre outil d'évaluation pour optimiser vos équipes !";
        }

        if (m.matches(".*(taille|combien|membre|personnes|effectif|nombre|size).*")) {
            return "👥 **Quelle taille pour votre équipe ?**\n\n" +
                    "• **2-3 personnes** : Idéal pour des petits projets rapides\n" +
                    "• **4-5 personnes** : Optimal pour la plupart des projets ✅\n" +
                    "• **6+ personnes** : Pour des projets complexes (organisation rigoureuse requise)\n\n" +
                    "📌 La taille idéale reste **3 à 5 membres** avec des rôles clairement définis.";
        }

        if (m.matches(".*(compétence|competence|skill|technologie|stack|langage|framework|tech).*")) {
            return "📊 **Compétences clés pour une équipe :**\n\n" +
                    "• **Frontend** : React, Angular, Vue.js\n" +
                    "• **Backend** : Spring Boot, Node.js, Python\n" +
                    "• **Base de données** : MySQL, MongoDB, PostgreSQL\n" +
                    "• **Outils** : Git, Docker, Méthodes Agiles\n\n" +
                    "🎯 Variez les profils pour maximiser l'efficacité !";
        }

        if (m.matches(".*(conflit|désaccord|desaccord|problème|probleme|dispute|tension|mésentente|mesentente).*")) {
            return "🕊️ **Gérer les conflits en équipe :**\n\n" +
                    "1. Écoute active de chaque partie\n" +
                    "2. Identifier la cause réelle\n" +
                    "3. Chercher une solution gagnant-gagnant\n" +
                    "4. Faire appel à un médiateur si besoin\n" +
                    "5. Établir des règles d'équipe claires\n\n" +
                    "💬 Le dialogue est toujours la meilleure solution !";
        }

        if (m.matches(".*(rôle|role|responsabilité|responsabilite|leader|chef|scrum|master|product owner).*")) {
            return "🎭 **Les rôles clés dans une équipe :**\n\n" +
                    "• **Leader / Chef de projet** : Coordination et prise de décision\n" +
                    "• **Développeur Frontend** : Interface utilisateur\n" +
                    "• **Développeur Backend** : Logique métier et APIs\n" +
                    "• **Designer UX/UI** : Expérience utilisateur\n" +
                    "• **Testeur QA** : Qualité et tests\n\n" +
                    "📌 En méthode Agile : Product Owner, Scrum Master, Dev Team.";
        }

        if (m.matches(".*(merci|thank|au revoir|bye|bonne journée|bonne journee|à bientôt|a bientot).*")) {
            return "😊 **Avec plaisir !**\n\n" +
                    "N'hésitez pas à revenir si vous avez d'autres questions.\n" +
                    "Bonne continuation dans vos projets d'équipe ! 🚀";
        }

        return "🤖 **Assistant B2U - Formation d'équipes**\n\n" +
                "Je suis là pour vous aider à créer des équipes performantes.\n\n" +
                "**Posez-moi des questions sur :**\n" +
                "• 📋 Quelles équipes sont disponibles\n" +
                "• ➕ Comment créer une équipe\n" +
                "• 🤝 Comment rejoindre une équipe\n" +
                "• 🛠️ Quelles compétences sont nécessaires\n" +
                "• 👥 Quelle taille d'équipe choisir\n" +
                "• 🎭 Quels rôles attribuer\n" +
                "• 🕊️ Comment gérer les conflits\n\n" +
                "À votre service ! 💪";
    }

    // ───────────────────────── LIAISON ENTREPRISE ─────────────────────────

    @PutMapping("/{equipeId}/associer-entreprise/{entrepriseId}")
    @Operation(summary = "Associer une entreprise à une équipe")
    public ResponseEntity<?> associerEntreprise(
            @PathVariable String equipeId,
            @PathVariable String entrepriseId) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);
            equipe.setEntrepriseId(entrepriseId);
            equipeService.modifierEquipe(equipe);
            return ResponseEntity.ok(Map.of(
                    "message",      "Entreprise associée avec succès",
                    "equipeId",     equipeId,
                    "entrepriseId", entrepriseId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/by-entreprise/{entrepriseId}")
    @Operation(summary = "Récupérer les équipes d'une entreprise")
    public ResponseEntity<?> getEquipesByEntreprise(
            @PathVariable String entrepriseId) {
        try {
            List<Equipe> equipes = equipeService.getEquipesByEntrepriseId(entrepriseId);
            return ResponseEntity.ok(equipes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ───────────────────────── LIAISON JIRA ─────────────────────────

    @Operation(summary = "Créer un projet Jira pour l'équipe (simulation)")
    @PostMapping("/{equipeId}/jira/create-project")
    public ResponseEntity<?> createJiraProject(@PathVariable String equipeId) {
        try {
            Equipe equipe = equipeService.getEquipeById(equipeId);

            String jiraKey = "B2U-" + equipe.getNomMembresEquipe()
                    .substring(0, Math.min(3, equipe.getNomMembresEquipe().length()))
                    .toUpperCase();

            List<Task> tasks = new ArrayList<>();
            Task task1 = new Task();
            task1.setTitle("Setup projet");
            task1.setStatus("DONE");
            tasks.add(task1);

            Task task2 = new Task();
            task2.setTitle("Frontend UI");
            task2.setStatus("IN_PROGRESS");
            tasks.add(task2);

            Task task3 = new Task();
            task3.setTitle("API Backend");
            task3.setStatus("TODO");
            tasks.add(task3);

            Task task4 = new Task();
            task4.setTitle("Tests & QA");
            task4.setStatus("TODO");
            tasks.add(task4);

            equipe.setJiraProjectKey(jiraKey);
            equipe.setTasks(tasks);
            equipeService.modifierEquipe(equipe);

            return ResponseEntity.ok(Map.of(
                    "projectKey",  jiraKey,
                    "projectName", equipe.getNomMembresEquipe(),
                    "status",      "created",
                    "board",       "https://jira.b2u.tn/projects/" + jiraKey,
                    "tasks",       tasks
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}