package tn.esprit.spring.b2u.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.Task;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.TaskRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {

    private final TaskRepo taskRepo;
    private final ProjetRepository projetRepository;

    @Override
    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    @Override
    public Optional<Task> getTaskById(String id) {
        return taskRepo.findById(id);
    }

    @Override
    public List<Task> getTasksByProjet(String projetId) {
        return taskRepo.findByProjetId(projetId);
    }

    @Override
    public List<Task> getTasksBySprint(String sprintId) {
        return taskRepo.findBySprintId(sprintId);
    }

    @Override
    public List<Task> getTasksByStatus(String status) {
        return taskRepo.findByStatus(status);
    }

    @Override
    public Task createTask(Task task) {
        return taskRepo.save(task);
    }

    @Override
    public Task updateTask(String id, Task task) {
        task.setId(id);
        return taskRepo.save(task);
    }

    @Override
    public Task updateTaskStatus(String id, String status) {
        Task task = taskRepo.findById(id).orElseThrow();
        task.setStatus(status);
        return taskRepo.save(task);
    }

    @Override
    public void deleteTask(String id) {
        taskRepo.deleteById(id);
    }

    @Override
    public List<Task> generateBacklogForProjet(String projetId) {
        Optional<Projet> projetOpt = projetRepository.findById(projetId);
        if (projetOpt.isEmpty()) return List.of();

        Projet projet = projetOpt.get();
        List<String> technologies = projet.getAllRequiredSkills();
        List<Task> tasks = new ArrayList<>();

        // Supprimer les anciennes tâches du projet
        List<Task> existingTasks = taskRepo.findByProjetId(projetId);
        taskRepo.deleteAll(existingTasks);

        // Tâches de base toujours présentes
        tasks.add(createTaskHelper(projetId, "Analyse et spécification des besoins",
                "Définir les besoins fonctionnels et non fonctionnels", 3));
        tasks.add(createTaskHelper(projetId, "Conception de l'architecture",
                "Concevoir l'architecture technique du projet", 3));

        // Tâches basées sur les technologies
        for (String tech : technologies) {
            String t = tech.toLowerCase();
            if (t.contains("angular")) {
                tasks.add(createTaskHelper(projetId, "Setup et configuration Angular",
                        "Initialiser le projet Angular avec les modules nécessaires", 2));
                tasks.add(createTaskHelper(projetId, "Développement des composants Angular",
                        "Créer les composants UI pour " + projet.getTitle(), 2));
            }
            if (t.contains("spring")) {
                tasks.add(createTaskHelper(projetId, "Configuration Spring Boot",
                        "Configurer Spring Boot, sécurité et dépendances", 3));
                tasks.add(createTaskHelper(projetId, "Développement des APIs REST",
                        "Implémenter les endpoints REST pour " + projet.getTitle(), 2));
            }
            if (t.contains("mongodb")) {
                tasks.add(createTaskHelper(projetId, "Conception du schéma MongoDB",
                        "Définir les collections et documents MongoDB", 2));
            }
            if (t.contains("react")) {
                tasks.add(createTaskHelper(projetId, "Setup React et composants UI",
                        "Initialiser React et créer les composants", 2));
            }
            if (t.contains("flutter")) {
                tasks.add(createTaskHelper(projetId, "Setup Flutter et navigation",
                        "Configurer Flutter et la navigation entre écrans", 2));
            }
            if (t.contains("python")) {
                tasks.add(createTaskHelper(projetId, "Setup environnement Python",
                        "Configurer l'environnement Python et les dépendances", 2));
            }
        }

        // Tâches finales toujours présentes
        tasks.add(createTaskHelper(projetId, "Tests unitaires et d'intégration",
                "Écrire et exécuter les tests pour " + projet.getTitle(), 1));
        tasks.add(createTaskHelper(projetId, "Déploiement et mise en production",
                "Déployer l'application sur le serveur de production", 1));

        return tasks;
    }

    private Task createTaskHelper(String projetId, String title,
                                  String description, int priority) {
        Task task = new Task();
        task.setProjetId(projetId);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus("todo");
        task.setPriority(priority);
        return taskRepo.save(task);
    }
}