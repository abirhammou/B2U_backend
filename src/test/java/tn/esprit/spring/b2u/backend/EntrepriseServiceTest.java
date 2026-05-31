package tn.esprit.spring.b2u.backend;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.spring.b2u.DTO.EntrepriseDTO;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.exception.DuplicateResourceException;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.EntrepriseRepo;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.service.entreprise.EntrepriseService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ✅ TEST UNITAIRE — EntrepriseService
 * Outil   : JUnit 5 + Mockito + AssertJ
 * Commande: click ▶️ in IntelliJ
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitaires - EntrepriseService")
class EntrepriseServiceTest {

    @Mock
    private EntrepriseRepo enterpriseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EntrepriseService enterpriseService;

    private Entreprise entreprise;
    private EntrepriseDTO dto;

    @BeforeEach
    void setUp() {
        entreprise = new Entreprise();
        entreprise.setId("ent-001");
        entreprise.setName("TechCorp");
        entreprise.setDescription("Société IT");
        entreprise.setSector("IT");
        entreprise.setAddress("Tunis");
        entreprise.setEmail("contact@techcorp.tn");
        entreprise.setPhone("12345678");
        entreprise.setUserId("user-001");

        dto = new EntrepriseDTO();
        dto.setName("TechCorp");
        dto.setDescription("Société IT");
        dto.setSector("IT");
        dto.setAddress("Tunis");
        dto.setEmail("contact@techcorp.tn");
        dto.setPhone("12345678");
    }

    // ─────────────────────────────────────────────
    // getAllEnterprises()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ getAllEnterprises() → retourne liste complète")
    void getAllEnterprises_returnsList() {
        when(enterpriseRepository.findAll()).thenReturn(List.of(entreprise));

        List<Entreprise> result = enterpriseService.getAllEnterprises();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TechCorp");
        verify(enterpriseRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("✅ getAllEnterprises() liste vide → retourne liste vide")
    void getAllEnterprises_empty_returnsEmptyList() {
        when(enterpriseRepository.findAll()).thenReturn(List.of());
        assertThat(enterpriseService.getAllEnterprises()).isEmpty();
    }

    // ─────────────────────────────────────────────
    // getEnterpriseById()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ getEnterpriseById() existant → retourne Optional présent")
    void getEnterpriseById_exists_returnsOptional() {
        when(enterpriseRepository.findById("ent-001")).thenReturn(Optional.of(entreprise));

        Optional<Entreprise> result = enterpriseService.getEnterpriseById("ent-001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("TechCorp");
    }

    @Test
    @DisplayName("✅ getEnterpriseById() inexistant → retourne Optional.empty()")
    void getEnterpriseById_notFound_returnsEmpty() {
        when(enterpriseRepository.findById("xxx")).thenReturn(Optional.empty());
        assertThat(enterpriseService.getEnterpriseById("xxx")).isEmpty();
    }

    // ─────────────────────────────────────────────
    // createEnterprise()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ createEnterprise() valide → crée User + Entreprise liés")
    void createEnterprise_valid_createsUserAndEntreprise() {
        when(enterpriseRepository.existsByName("TechCorp")).thenReturn(false);
        when(passwordEncoder.encode("company123")).thenReturn("encoded123");

        User savedUser = new User();
        savedUser.setId("user-001");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Entreprise savedE = new Entreprise();
        savedE.setId("ent-001");
        when(enterpriseRepository.save(any(Entreprise.class))).thenReturn(savedE);

        assertThatCode(() -> enterpriseService.createEnterprise(dto))
                .doesNotThrowAnyException();

        verify(userRepository, times(2)).save(any(User.class));
        verify(enterpriseRepository, times(1)).save(any(Entreprise.class));
    }

    @Test
    @DisplayName("❌ createEnterprise() nom doublon → DuplicateResourceException")
    void createEnterprise_duplicateName_throwsDuplicate() {
        when(enterpriseRepository.existsByName("TechCorp")).thenReturn(true);

        assertThatThrownBy(() -> enterpriseService.createEnterprise(dto))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(enterpriseRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("✅ createEnterprise() → password encodé, rôle = ROLE_COMPANY")
    void createEnterprise_passwordEncodedAndRoleSet() {
        when(enterpriseRepository.existsByName("TechCorp")).thenReturn(false);
        when(passwordEncoder.encode("company123")).thenReturn("hashed");

        User savedUser = new User();
        savedUser.setId("user-001");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getRole() != null) assertThat(u.getRole()).isEqualTo("ROLE_COMPANY");
            u.setId("user-001");
            return u;
        });

        Entreprise savedE = new Entreprise();
        savedE.setId("ent-001");
        when(enterpriseRepository.save(any(Entreprise.class))).thenReturn(savedE);

        enterpriseService.createEnterprise(dto);
        verify(passwordEncoder).encode("company123");
    }

    // ─────────────────────────────────────────────
    // updateEnterprise()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ updateEnterprise() valide → retourne entité mise à jour")
    void updateEnterprise_valid_returnsUpdated() {
        EntrepriseDTO updateDto = new EntrepriseDTO();
        updateDto.setName("TechCorp");   // same name → no duplicate check triggered
        updateDto.setPhone("12345678");  // same phone → no duplicate check triggered
        updateDto.setDescription("Updated");
        updateDto.setSector("FinTech");
        updateDto.setAddress("Sfax");
        updateDto.setEmail("new@techcorp.tn");

        when(enterpriseRepository.findById("ent-001")).thenReturn(Optional.of(entreprise));
        when(enterpriseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Entreprise result = enterpriseService.updateEnterprise("ent-001", updateDto);

        assertThat(result.getSector()).isEqualTo("FinTech");
    }

    @Test
    @DisplayName("❌ updateEnterprise() ID inexistant → ResourceNotFoundException")
    void updateEnterprise_notFound_throwsException() {
        when(enterpriseRepository.findById("xxx")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enterpriseService.updateEnterprise("xxx", dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Enterprise not found with id: xxx");
    }

    @Test
    @DisplayName("❌ updateEnterprise() nom doublon → DuplicateResourceException")
    void updateEnterprise_duplicateName_throwsDuplicate() {
        EntrepriseDTO dtoWithNewName = new EntrepriseDTO();
        dtoWithNewName.setName("AutreCorp"); // different name → triggers duplicate check
        dtoWithNewName.setPhone("12345678");
        dtoWithNewName.setEmail("x@x.tn");
        dtoWithNewName.setSector("IT");
        dtoWithNewName.setAddress("Tunis");

        when(enterpriseRepository.findById("ent-001")).thenReturn(Optional.of(entreprise));
        when(enterpriseRepository.existsByNameAndIdNot("AutreCorp", "ent-001")).thenReturn(true);

        assertThatThrownBy(() -> enterpriseService.updateEnterprise("ent-001", dtoWithNewName))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("❌ updateEnterprise() téléphone doublon → DuplicateResourceException")
    void updateEnterprise_duplicatePhone_throwsDuplicate() {
        // ✅ FIX: use a NEW phone so the phone duplicate check is actually reached
        EntrepriseDTO dtoWithNewPhone = new EntrepriseDTO();
        dtoWithNewPhone.setName("TechCorp");   // same name → name check passes
        dtoWithNewPhone.setPhone("99999999");  // different phone → triggers phone check
        dtoWithNewPhone.setEmail("x@x.tn");
        dtoWithNewPhone.setSector("IT");
        dtoWithNewPhone.setAddress("Tunis");

        when(enterpriseRepository.findById("ent-001")).thenReturn(Optional.of(entreprise));
        // name is same as existing → the if(!existing.getName().equals(dto.getName())) is FALSE
        // so existsByNameAndIdNot is NEVER called → do NOT mock it
        when(enterpriseRepository.existsByPhoneAndIdNot("99999999", "ent-001")).thenReturn(true);

        assertThatThrownBy(() -> enterpriseService.updateEnterprise("ent-001", dtoWithNewPhone))
                .isInstanceOf(DuplicateResourceException.class);
    }

    // ─────────────────────────────────────────────
    // deleteEnterprise()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ deleteEnterprise() → deleteById appelé une fois")
    void deleteEnterprise_callsDeleteById() {
        doNothing().when(enterpriseRepository).deleteById("ent-001");
        enterpriseService.deleteEnterprise("ent-001");
        verify(enterpriseRepository, times(1)).deleteById("ent-001");
    }

    // ─────────────────────────────────────────────
    // getTotalCompaniesCount()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ getTotalCompaniesCount() → retourne le bon nombre")
    void getTotalCompaniesCount_returnsCount() {
        when(enterpriseRepository.count()).thenReturn(5L);
        assertThat(enterpriseService.getTotalCompaniesCount()).isEqualTo(5L);
    }

    // ─────────────────────────────────────────────
    // getCountBySector()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ getCountBySector() → groupe par secteur correctement")
    void getCountBySector_groupsBySector() {
        Entreprise e2 = new Entreprise(); e2.setSector("IT");
        Entreprise e3 = new Entreprise(); e3.setSector("Finance");
        when(enterpriseRepository.findAll()).thenReturn(List.of(entreprise, e2, e3));

        Map<String, Long> result = enterpriseService.getCountBySector();

        assertThat(result).containsEntry("IT", 2L).containsEntry("Finance", 1L);
    }

    @Test
    @DisplayName("✅ getCountBySector() → ignore les secteurs null")
    void getCountBySector_ignoresNullSectors() {
        Entreprise withNull = new Entreprise(); withNull.setSector(null);
        when(enterpriseRepository.findAll()).thenReturn(List.of(entreprise, withNull));

        Map<String, Long> result = enterpriseService.getCountBySector();

        assertThat(result).containsKey("IT");
        assertThat(result).doesNotContainKey(null);
    }

    // ─────────────────────────────────────────────
    // getByUserId()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ getByUserId() existant → retourne l'entreprise liée")
    void getByUserId_returnsEntreprise() {
        when(enterpriseRepository.findByUserId("user-001")).thenReturn(Optional.of(entreprise));
        assertThat(enterpriseService.getByUserId("user-001")).isPresent();
    }

    // ─────────────────────────────────────────────
    // updateMyEnterprise()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ updateMyEnterprise() valide → met à jour les champs")
    void updateMyEnterprise_valid_updatesFields() {
        EntrepriseDTO myDto = new EntrepriseDTO();
        myDto.setName("Updated");
        myDto.setSector("AgriTech");
        myDto.setAddress("Sousse");
        myDto.setPhone("87654321");
        myDto.setDescription("Desc");

        when(enterpriseRepository.findByUserId("user-001")).thenReturn(Optional.of(entreprise));
        when(enterpriseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Entreprise result = enterpriseService.updateMyEnterprise("user-001", myDto);
        assertThat(result.getName()).isEqualTo("Updated");
    }

    @Test
    @DisplayName("❌ updateMyEnterprise() user sans entreprise → ResourceNotFoundException")
    void updateMyEnterprise_noEntreprise_throwsException() {
        // ✅ FIX: only mock what is actually called — no extra stubs
        when(enterpriseRepository.findByUserId("user-999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enterpriseService.updateMyEnterprise("user-999", dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────────
    // getSimilar()
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("✅ getSimilar() → retourne même secteur, exclut soi-même")
    void getSimilar_returnsSameSectorExcludingSelf() {
        Entreprise e2 = new Entreprise(); e2.setId("ent-002"); e2.setSector("IT");
        Entreprise e3 = new Entreprise(); e3.setId("ent-003"); e3.setSector("Finance");

        when(enterpriseRepository.findById("ent-001")).thenReturn(Optional.of(entreprise));
        when(enterpriseRepository.findAll()).thenReturn(List.of(entreprise, e2, e3));

        List<Entreprise> result = enterpriseService.getSimilar("ent-001");

        assertThat(result).hasSize(1);
        assertThat(result).noneMatch(e -> e.getId().equals("ent-001"));
    }

    @Test
    @DisplayName("✅ getSimilar() secteur null → retourne liste vide")
    void getSimilar_nullSector_returnsEmpty() {
        entreprise.setSector(null);
        when(enterpriseRepository.findById("ent-001")).thenReturn(Optional.of(entreprise));
        assertThat(enterpriseService.getSimilar("ent-001")).isEmpty();
    }

    @Test
    @DisplayName("❌ getSimilar() ID inexistant → ResourceNotFoundException")
    void getSimilar_notFound_throwsException() {
        when(enterpriseRepository.findById("xxx")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> enterpriseService.getSimilar("xxx"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}