package tn.esprit.spring.b2u;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.b2u.entity.Equipe;
import tn.esprit.spring.b2u.entity.Task;
import tn.esprit.spring.b2u.repository.EquipeRepo;
import tn.esprit.spring.b2u.service.equipe.EquipeService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    @Mock
    private EquipeRepo equipeRepo;

    @InjectMocks
    private EquipeService equipeService;

    private Equipe equipe;

    @BeforeEach
    void setUp() {
        equipe = Equipe.builder()
                .idEquipe("equipe-001")
                .nomMembresEquipe("Team Alpha")
                .descriptionProfil("Équipe fullstack")
                .build();
    }

    // ─── Test 1 : Récupérer toutes les équipes ───
    @Test
    void testGetAllEquipes() {
        // GIVEN
        List<Equipe> equipes = Arrays.asList(equipe);
        when(equipeRepo.findAll()).thenReturn(equipes);

        // WHEN
        List<Equipe> result = equipeService.getAllEquipes();

        // THEN
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Team Alpha", result.get(0).getNomMembresEquipe());
        verify(equipeRepo, times(1)).findAll();
    }

    // ─── Test 2 : Récupérer équipe par ID ───
    @Test
    void testGetEquipeById() {
        // GIVEN
        when(equipeRepo.findById("equipe-001")).thenReturn(Optional.of(equipe));

        // WHEN
        Equipe result = equipeService.getEquipeById("equipe-001");

        // THEN
        assertNotNull(result);
        assertEquals("equipe-001", result.getIdEquipe());
        assertEquals("Team Alpha", result.getNomMembresEquipe());
    }

    // ─── Test 3 : Équipe introuvable ───
    @Test
    void testGetEquipeByIdNotFound() {
        // GIVEN
        when(equipeRepo.findById("wrong-id")).thenReturn(Optional.empty());

        // THEN
        assertThrows(RuntimeException.class, () -> {
            equipeService.getEquipeById("wrong-id");
        });
    }

    // ─── Test 4 : Modifier une équipe ───
    @Test
    void testModifierEquipe() {
        // GIVEN
        equipe.setNomMembresEquipe("Team Beta");
        when(equipeRepo.save(equipe)).thenReturn(equipe);

        // WHEN
        Equipe result = equipeService.modifierEquipe(equipe);

        // THEN
        assertNotNull(result);
        assertEquals("Team Beta", result.getNomMembresEquipe());
        verify(equipeRepo, times(1)).save(equipe);
    }

    // ─── Test 5 : Supprimer une équipe ───
    @Test
    void testSupprimerEquipe() {
        // GIVEN
        doNothing().when(equipeRepo).deleteById("equipe-001");

        // WHEN
        equipeService.supprimerEquipe("equipe-001");

        // THEN
        verify(equipeRepo, times(1)).deleteById("equipe-001");
    }

    // ─── Test 6 : Équipe avec tâches (FIXED - using Task entity correctly) ───
    @Test
    void testEquipeAvecTaches() {
        // GIVEN
        // Create tasks using builder pattern
        Task task1 = Task.builder()
                .title("Setup projet")
                .status("DONE")
                .assignedTo("student-001")
                .description("Setup project description")
                .build();

        Task task2 = Task.builder()
                .title("API Backend")
                .status("TODO")
                .assignedTo("student-002")
                .description("API Backend description")
                .build();

        equipe.setTasks(Arrays.asList(task1, task2));

        when(equipeRepo.findById("equipe-001")).thenReturn(Optional.of(equipe));

        // WHEN
        Equipe result = equipeService.getEquipeById("equipe-001");

        // THEN
        assertNotNull(result.getTasks());
        assertEquals(2, result.getTasks().size());

        // Check first task
        assertEquals("Setup projet", result.getTasks().get(0).getTitle());
        assertEquals("DONE", result.getTasks().get(0).getStatus());
        assertEquals("student-001", result.getTasks().get(0).getAssignedTo());
        assertEquals("Setup project description", result.getTasks().get(0).getDescription());

        // Check second task
        assertEquals("API Backend", result.getTasks().get(1).getTitle());
        assertEquals("TODO", result.getTasks().get(1).getStatus());
        assertEquals("student-002", result.getTasks().get(1).getAssignedTo());
        assertEquals("API Backend description", result.getTasks().get(1).getDescription());
    }

    // ─── Test 7 : Associer entreprise ───
    @Test
    void testAssocierEntreprise() {
        // GIVEN
        equipe.setEntrepriseId("entreprise-123");
        when(equipeRepo.save(equipe)).thenReturn(equipe);

        // WHEN
        Equipe result = equipeService.modifierEquipe(equipe);

        // THEN
        assertEquals("entreprise-123", result.getEntrepriseId());
        verify(equipeRepo, times(1)).save(equipe);
    }
}