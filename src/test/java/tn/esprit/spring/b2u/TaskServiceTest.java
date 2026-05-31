package tn.esprit.spring.b2u;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.Task;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.TaskRepo;
import tn.esprit.spring.b2u.service.task.TaskService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepo taskRepo;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllTasks() {
        Task t1 = new Task();
        t1.setId("1");
        t1.setTitle("Task 1");

        Task t2 = new Task();
        t2.setId("2");
        t2.setTitle("Task 2");

        when(taskRepo.findAll()).thenReturn(Arrays.asList(t1, t2));

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepo, times(1)).findAll();
    }

    @Test
    void testGetTasksByProjet() {
        Task t1 = new Task();
        t1.setProjetId("p1");
        t1.setTitle("Task 1");

        when(taskRepo.findByProjetId("p1")).thenReturn(Arrays.asList(t1));

        List<Task> result = taskService.getTasksByProjet("p1");

        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getProjetId());
    }

    @Test
    void testCreateTask() {
        Task task = new Task();
        task.setTitle("Nouvelle Task");
        task.setStatus("todo");
        task.setPriority(2);

        when(taskRepo.save(task)).thenReturn(task);

        Task result = taskService.createTask(task);

        assertNotNull(result);
        assertEquals("Nouvelle Task", result.getTitle());
        assertEquals("todo", result.getStatus());
        verify(taskRepo, times(1)).save(task);
    }

    @Test
    void testUpdateTaskStatus() {
        Task task = new Task();
        task.setId("1");
        task.setStatus("todo");

        when(taskRepo.findById("1")).thenReturn(Optional.of(task));
        when(taskRepo.save(any(Task.class))).thenReturn(task);

        Task result = taskService.updateTaskStatus("1", "in-progress");

        assertEquals("in-progress", result.getStatus());
        verify(taskRepo, times(1)).save(any(Task.class));
    }

    @Test
    void testDeleteTask() {
        doNothing().when(taskRepo).deleteById("1");

        taskService.deleteTask("1");

        verify(taskRepo, times(1)).deleteById("1");
    }

    @Test
    void testGenerateBacklogProjetNotFound() {
        when(projetRepository.findById("999")).thenReturn(Optional.empty());

        List<Task> result = taskService.generateBacklogForProjet("999");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateBacklogForProjet() {
        Projet projet = new Projet();
        projet.setId("p1");
        projet.setTitle("Test Projet");
        projet.setRequiredSkills(Arrays.asList("Angular", "Spring Boot", "MongoDB"));

        Task task = new Task();
        task.setProjetId("p1");
        task.setTitle("Analyse et spécification des besoins");

        when(projetRepository.findById("p1")).thenReturn(Optional.of(projet));
        when(taskRepo.findByProjetId("p1")).thenReturn(Arrays.asList());
        when(taskRepo.save(any(Task.class))).thenReturn(task);

        List<Task> result = taskService.generateBacklogForProjet("p1");

        assertNotNull(result);
        verify(taskRepo, atLeastOnce()).save(any(Task.class));
    }
}
