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
        List<Task> tasks = new ArrayList<>();

        // Générer des tâches automatiquement basées sur le projet
        String[] defaultTasks = {
                "Analyse des besoins",
                "Conception de l'architecture",
                "Développement Backend",
                "Développement Frontend",
                "Tests unitaires",
                "Déploiement"
        };

        for (int i = 0; i < defaultTasks.length; i++) {
            Task task = new Task();
            task.setProjetId(projetId);
            task.setTitle(defaultTasks[i]);
            task.setDescription("Tâche générée automatiquement pour " + projet.getTitle());
            task.setStatus("todo");
            task.setPriority(i < 2 ? 3 : i < 4 ? 2 : 1);
            tasks.add(taskRepo.save(task));
        }
        return tasks;
    }
}