package tn.esprit.spring.b2u;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.Sprint;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.SprintRepo;
import tn.esprit.spring.b2u.service.sprint.SprintService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SprintServiceTest {

    @Mock
    private SprintRepo sprintRepo;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private SprintService sprintService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllSprints() {
        Sprint s1 = new Sprint();
        s1.setId("1");
        s1.setName("Sprint 1");

        Sprint s2 = new Sprint();
        s2.setId("2");
        s2.setName("Sprint 2");

        when(sprintRepo.findAll()).thenReturn(Arrays.asList(s1, s2));

        List<Sprint> result = sprintService.getAllSprints();

        assertEquals(2, result.size());
        verify(sprintRepo, times(1)).findAll();
    }

    @Test
    void testGetSprintsByProjet() {
        Sprint s1 = new Sprint();
        s1.setProjetId("p1");
        s1.setName("Sprint 1");

        when(sprintRepo.findByProjetId("p1")).thenReturn(Arrays.asList(s1));

        List<Sprint> result = sprintService.getSprintsByProjet("p1");

        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getProjetId());
    }

    @Test
    void testCreateSprint() {
        Sprint sprint = new Sprint();
        sprint.setName("Sprint 1");
        sprint.setStatus("active");

        when(sprintRepo.save(sprint)).thenReturn(sprint);

        Sprint result = sprintService.createSprint(sprint);

        assertNotNull(result);
        assertEquals("Sprint 1", result.getName());
        verify(sprintRepo, times(1)).save(sprint);
    }

    @Test
    void testDeleteSprint() {
        doNothing().when(sprintRepo).deleteById("1");

        sprintService.deleteSprint("1");

        verify(sprintRepo, times(1)).deleteById("1");
    }

    @Test
    void testGenerateSprintsProjetNotFound() {
        when(projetRepository.findById("999")).thenReturn(Optional.empty());

        List<Sprint> result = sprintService.generateSprintsForProjet("999");

        assertTrue(result.isEmpty());
    }

    @Test
    void testGenerateSprintsForProjet() {
        Projet projet = new Projet();
        projet.setId("p1");
        projet.setTitle("Test Projet");

        // Deadline dans 30 jours
        Date deadline = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
        projet.setDeadline(deadline);

        Sprint sprint = new Sprint();
        sprint.setProjetId("p1");
        sprint.setName("Sprint 1");

        when(projetRepository.findById("p1")).thenReturn(Optional.of(projet));
        when(sprintRepo.findByProjetId("p1")).thenReturn(Arrays.asList());
        when(sprintRepo.save(any(Sprint.class))).thenReturn(sprint);

        List<Sprint> result = sprintService.generateSprintsForProjet("p1");

        assertNotNull(result);
        verify(sprintRepo, atLeastOnce()).save(any(Sprint.class));
    }
}
