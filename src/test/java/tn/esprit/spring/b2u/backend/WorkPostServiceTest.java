package tn.esprit.spring.b2u.backend;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.WorkPostStatus;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.WorkPostRepo;
import tn.esprit.spring.b2u.service.workPost.WorkPostService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ TEST UNITAIRE — WorkPostService
 *
 * Type    : Unitaire (JUnit 5 + Mockito, NO Spring context, NO MockMvc)
 * Outil   : JUnit 5 + Mockito + AssertJ
 * Commande: .\mvnw test -Dtest=WorkPostServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires - WorkPostService")
class WorkPostServiceTest {

    @Mock
    private WorkPostRepo workPostRepo;

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private WorkPostService workPostService;

    private WorkPost workPost;
    private Projet projet;

    @BeforeEach
    void setUp() {
        workPost = new WorkPost();
        workPost.setId("wp-001");
        workPost.setEntrepriseId("ent-001");
        workPost.setTitle("Développeur Full Stack");
        workPost.setHoursPerWeek(20);
        workPost.setRequiredSkills("Java, Angular");
        workPost.setStatus(WorkPostStatus.ACTIVE);
        workPost.setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
        workPost.setProjetId(null);

        projet = new Projet();
        projet.setId("proj-001");
        projet.setTitle("Projet Alpha");
    }

    // ─────────────────────────────────────────────
    // create()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] create() → status forcé ACTIVE, createdAt défini")
    void create_setsStatusActiveAndCreatedAt() {
        WorkPost input = new WorkPost();
        input.setTitle("New Post");
        input.setHoursPerWeek(15);
        input.setEntrepriseId("ent-001");

        when(workPostRepo.save(any(WorkPost.class))).thenAnswer(inv -> inv.getArgument(0));

        WorkPost result = workPostService.create(input);

        assertThat(result.getStatus()).isEqualTo(WorkPostStatus.ACTIVE);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(workPostRepo).save(input);
    }

    @Test
    @DisplayName("✅ [UNIT] create() → retourne le workpost sauvegardé")
    void create_returnsSavedWorkPost() {
        when(workPostRepo.save(any())).thenReturn(workPost);
        WorkPost result = workPostService.create(workPost);
        assertThat(result.getTitle()).isEqualTo("Développeur Full Stack");
        assertThat(result.getEntrepriseId()).isEqualTo("ent-001");
    }

    // ─────────────────────────────────────────────
    // getAll()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] getAll() → retourne tous les workposts")
    void getAll_returnsAll() {
        WorkPost wp2 = new WorkPost(); wp2.setId("wp-002");
        when(workPostRepo.findAll()).thenReturn(List.of(workPost, wp2));

        assertThat(workPostService.getAll()).hasSize(2);
        verify(workPostRepo).findAll();
    }

    @Test
    @DisplayName("✅ [UNIT] getAll() liste vide → retourne liste vide")
    void getAll_empty_returnsEmpty() {
        when(workPostRepo.findAll()).thenReturn(List.of());
        assertThat(workPostService.getAll()).isEmpty();
    }

    // ─────────────────────────────────────────────
    // getByEntreprise()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] getByEntreprise() → retourne les posts de l'entreprise")
    void getByEntreprise_returnsFiltered() {
        when(workPostRepo.findByEntrepriseId("ent-001")).thenReturn(List.of(workPost));

        List<WorkPost> result = workPostService.getByEntreprise("ent-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntrepriseId()).isEqualTo("ent-001");
    }

    @Test
    @DisplayName("✅ [UNIT] getByEntreprise() entreprise sans posts → liste vide")
    void getByEntreprise_noPost_returnsEmpty() {
        when(workPostRepo.findByEntrepriseId("ent-999")).thenReturn(List.of());
        assertThat(workPostService.getByEntreprise("ent-999")).isEmpty();
    }

    // ─────────────────────────────────────────────
    // getById()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] getById() existant → retourne le workpost")
    void getById_exists_returns() {
        when(workPostRepo.findById("wp-001")).thenReturn(Optional.of(workPost));

        WorkPost result = workPostService.getById("wp-001");

        assertThat(result.getId()).isEqualTo("wp-001");
        assertThat(result.getTitle()).isEqualTo("Développeur Full Stack");
    }

    @Test
    @DisplayName("❌ [UNIT] getById() inexistant → RuntimeException")
    void getById_notFound_throwsException() {
        when(workPostRepo.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPostService.getById("xxx"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("WorkPost not found: xxx");
    }

    // ─────────────────────────────────────────────
    // update()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] update() → conserve createdAt original")
    void update_preservesCreatedAt() {
        LocalDateTime original = workPost.getCreatedAt();
        WorkPost input = new WorkPost();
        input.setTitle("Lead Dev");
        input.setStatus(WorkPostStatus.ACTIVE);

        when(workPostRepo.findById("wp-001")).thenReturn(Optional.of(workPost));
        when(workPostRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkPost result = workPostService.update("wp-001", input);

        assertThat(result.getCreatedAt()).isEqualTo(original);
        assertThat(result.getId()).isEqualTo("wp-001");
    }

    @Test
    @DisplayName("✅ [UNIT] update() status null → conserve status existant")
    void update_nullStatus_keepsExisting() {
        WorkPost input = new WorkPost();
        input.setTitle("Updated");
        input.setStatus(null);

        when(workPostRepo.findById("wp-001")).thenReturn(Optional.of(workPost));
        when(workPostRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkPost result = workPostService.update("wp-001", input);

        assertThat(result.getStatus()).isEqualTo(WorkPostStatus.ACTIVE);
    }

    @Test
    @DisplayName("❌ [UNIT] update() ID inexistant → RuntimeException")
    void update_notFound_throwsException() {
        when(workPostRepo.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPostService.update("xxx", new WorkPost()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("WorkPost not found: xxx");
    }

    // ─────────────────────────────────────────────
    // delete()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] delete() → deleteById appelé une fois")
    void delete_callsDeleteById() {
        doNothing().when(workPostRepo).deleteById("wp-001");
        workPostService.delete("wp-001");
        verify(workPostRepo, times(1)).deleteById("wp-001");
    }

    // ─────────────────────────────────────────────
    // getRecommendedPosts()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] getRecommendedPosts(20) → retourne posts ≤ 20h/semaine")
    void getRecommendedPosts_filtersCorrectly() {
        WorkPost wp10 = new WorkPost(); wp10.setHoursPerWeek(10);
        WorkPost wp25 = new WorkPost(); wp25.setHoursPerWeek(25);

        when(workPostRepo.findAll()).thenReturn(List.of(workPost, wp10, wp25));

        List<WorkPost> result = workPostService.getRecommendedPosts(20);

        // workPost=20h ✅, wp10=10h ✅, wp25=25h ❌
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getHoursPerWeek() <= 20);
    }

    @Test
    @DisplayName("✅ [UNIT] getRecommendedPosts(0) → retourne liste vide")
    void getRecommendedPosts_maxZero_returnsEmpty() {
        when(workPostRepo.findAll()).thenReturn(List.of(workPost));
        assertThat(workPostService.getRecommendedPosts(0)).isEmpty();
    }

    // ─────────────────────────────────────────────
    // assignProjet()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] assignProjet() → lie projet et copie titre")
    void assignProjet_setsProjetIdAndTitle() {
        when(workPostRepo.findById("wp-001")).thenReturn(Optional.of(workPost));
        when(projetRepository.findById("proj-001")).thenReturn(Optional.of(projet));
        when(workPostRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkPost result = workPostService.assignProjet("wp-001", "proj-001");

        assertThat(result.getProjetId()).isEqualTo("proj-001");
        assertThat(result.getProjetTitle()).isEqualTo("Projet Alpha");
    }

    @Test
    @DisplayName("❌ [UNIT] assignProjet() workpost inexistant → RuntimeException")
    void assignProjet_workpostNotFound_throwsException() {
        when(workPostRepo.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPostService.assignProjet("xxx", "proj-001"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("WorkPost not found");
    }

    @Test
    @DisplayName("❌ [UNIT] assignProjet() projet inexistant → RuntimeException")
    void assignProjet_projetNotFound_throwsException() {
        when(workPostRepo.findById("wp-001")).thenReturn(Optional.of(workPost));
        when(projetRepository.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPostService.assignProjet("wp-001", "xxx"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Projet not found");
    }

    // ─────────────────────────────────────────────
    // unassignProjet()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] unassignProjet() → met projetId à null")
    void unassignProjet_setsProjetIdNull() {
        workPost.setProjetId("proj-001");
        when(workPostRepo.findById("wp-001")).thenReturn(Optional.of(workPost));
        when(workPostRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        WorkPost result = workPostService.unassignProjet("wp-001");

        assertThat(result.getProjetId()).isNull();
    }

    @Test
    @DisplayName("❌ [UNIT] unassignProjet() inexistant → RuntimeException")
    void unassignProjet_notFound_throwsException() {
        when(workPostRepo.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workPostService.unassignProjet("xxx"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("WorkPost not found: xxx");
    }

    // ─────────────────────────────────────────────
    // getByProjet()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ [UNIT] getByProjet() → retourne les posts liés au projet")
    void getByProjet_returnsLinkedPosts() {
        workPost.setProjetId("proj-001");
        when(workPostRepo.findByProjetId("proj-001")).thenReturn(List.of(workPost));

        List<WorkPost> result = workPostService.getByProjet("proj-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjetId()).isEqualTo("proj-001");
    }
}