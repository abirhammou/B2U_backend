package tn.esprit.spring.b2u.backend;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.WorkPostStatus;
import tn.esprit.spring.b2u.repository.EntrepriseRepo;
import tn.esprit.spring.b2u.repository.WorkPostRepo;
import tn.esprit.spring.b2u.service.entreprise.EntrepriseService;
import tn.esprit.spring.b2u.service.workPost.WorkPostService;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ✅ TEST D'INTÉGRATION — Entreprise + WorkPost
 *
 * Utilise MongoDB LOCAL (localhost:27017)
 * Base de données de test : b2u_test (séparée de la prod)
 *
 * PRÉREQUIS : MongoDB doit tourner localement sur localhost:27017
 * Pour installer MongoDB : https://www.mongodb.com/try/download/community (gratuit)
 *
 * Commande : click ▶️ in IntelliJ on this class
 */

//@Disabled("MongoDB local non disponible - à activer quand MongoDB est installé")
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Tests d'Intégration — Entreprise ↔ WorkPost (MongoDB local)")
class EntrepriseWorkpostIntegrationTest {

    @Autowired
    private EntrepriseService enterpriseService;

    @Autowired
    private WorkPostService workPostService;

    @Autowired
    private EntrepriseRepo entrepriseRepo;

    @Autowired
    private WorkPostRepo workPostRepo;

    @BeforeEach
    void cleanUp() {
        // Clean test database before each test
        workPostRepo.deleteAll();
        entrepriseRepo.deleteAll();
    }

    @AfterAll
    static void finalCleanup(@Autowired EntrepriseRepo eRepo,
                             @Autowired WorkPostRepo wRepo) {
        // Clean up test data after all tests
        wRepo.deleteAll();
        eRepo.deleteAll();
    }

    // ─────────────────────────────────────────────
    // CRUD complet WorkPost
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("✅ [INTEG] Créer workpost → persiste en MongoDB local")
    void createWorkpost_persistsInLocalMongoDB() {
        Entreprise e = new Entreprise();
        e.setName("IntegCorp");
        e.setSector("IT");
        Entreprise savedE = entrepriseRepo.save(e);

        WorkPost wp = new WorkPost();
        wp.setTitle("Dev Java");
        wp.setHoursPerWeek(20);
        wp.setEntrepriseId(savedE.getId());

        WorkPost savedWp = workPostService.create(wp);

        assertThat(savedWp.getId()).isNotNull();
        assertThat(savedWp.getEntrepriseId()).isEqualTo(savedE.getId());
        assertThat(savedWp.getStatus()).isEqualTo(WorkPostStatus.ACTIVE);
        assertThat(savedWp.getCreatedAt()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("✅ [INTEG] Lier workpost à une entreprise → relation cohérente")
    void createWorkpost_linkedToEntreprise_isCoherent() {
        Entreprise e = new Entreprise();
        e.setName("TestCorp");
        e.setSector("IT");
        Entreprise saved = entrepriseRepo.save(e);

        WorkPost wp = new WorkPost();
        wp.setTitle("Dev Angular");
        wp.setHoursPerWeek(15);
        wp.setEntrepriseId(saved.getId());
        workPostService.create(wp);

        List<WorkPost> posts = workPostService.getByEntreprise(saved.getId());
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("Dev Angular");
    }

    @Test
    @Order(3)
    @DisplayName("✅ [INTEG] CRUD complet WorkPost en MongoDB local")
    void fullCrudWorkPost_integration() {
        Entreprise e = new Entreprise();
        e.setName("CrudCorp");
        Entreprise savedE = entrepriseRepo.save(e);

        // CREATE
        WorkPost wp = new WorkPost();
        wp.setTitle("Poste Initial");
        wp.setHoursPerWeek(15);
        wp.setEntrepriseId(savedE.getId());
        WorkPost created = workPostService.create(wp);
        assertThat(created.getId()).isNotNull();

        // READ
        WorkPost found = workPostService.getById(created.getId());
        assertThat(found.getTitle()).isEqualTo("Poste Initial");

        // UPDATE
        WorkPost updateInput = new WorkPost();
        updateInput.setTitle("Poste Updated");
        updateInput.setHoursPerWeek(30);
        WorkPost updated = workPostService.update(created.getId(), updateInput);
        assertThat(updated.getTitle()).isEqualTo("Poste Updated");
        assertThat(updated.getHoursPerWeek()).isEqualTo(30);

        // DELETE
        workPostService.delete(updated.getId());
        assertThatThrownBy(() -> workPostService.getById(updated.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Order(4)
    @DisplayName("✅ [INTEG] getRecommendedPosts → filtre par heures en base réelle")
    void getRecommendedPosts_realDb_filtersByHours() {
        Entreprise e = new Entreprise();
        e.setName("RecomCorp");
        Entreprise savedE = entrepriseRepo.save(e);

        WorkPost wp10 = new WorkPost();
        wp10.setTitle("Post 10h"); wp10.setHoursPerWeek(10);
        wp10.setEntrepriseId(savedE.getId());

        WorkPost wp20 = new WorkPost();
        wp20.setTitle("Post 20h"); wp20.setHoursPerWeek(20);
        wp20.setEntrepriseId(savedE.getId());

        WorkPost wp30 = new WorkPost();
        wp30.setTitle("Post 30h"); wp30.setHoursPerWeek(30);
        wp30.setEntrepriseId(savedE.getId());

        workPostService.create(wp10);
        workPostService.create(wp20);
        workPostService.create(wp30);

        List<WorkPost> result = workPostService.getRecommendedPosts(20);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getHoursPerWeek() <= 20);
    }
}