package tn.esprit.spring.b2u.service.task;

import tn.esprit.spring.b2u.entity.Task;
import java.util.List;
import java.util.Optional;

public interface ITaskService {
    List<Task> getAllTasks();
    Optional<Task> getTaskById(String id);
    List<Task> getTasksByProjet(String projetId);
    List<Task> getTasksBySprint(String sprintId);
    List<Task> getTasksByStatus(String status);
    Task createTask(Task task);
    Task updateTask(String id, Task task);
    Task updateTaskStatus(String id, String status);
    void deleteTask(String id);
    List<Task> generateBacklogForProjet(String projetId);
}
