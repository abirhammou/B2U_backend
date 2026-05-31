package tn.esprit.spring.b2u;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.service.projet.ProjetService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjetServiceTest {

    @Mock
    private ProjetRepository projetRepository;

    @InjectMocks
    private ProjetService projetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProjets() {
        Projet p1 = new Projet();
        p1.setId("1");
        p1.setTitle("Projet 1");

        Projet p2 = new Projet();
        p2.setId("2");
        p2.setTitle("Projet 2");

        when(projetRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Projet> result = projetService.getAllProjets();

        assertEquals(2, result.size());
        verify(projetRepository, times(1)).findAll();
    }

    @Test
    void testGetProjetById() {
        Projet projet = new Projet();
        projet.setId("1");
        projet.setTitle("Test Projet");

        when(projetRepository.findById("1")).thenReturn(Optional.of(projet));

        Optional<Projet> result = projetService.getProjetById("1");

        assertTrue(result.isPresent());
        assertEquals("Test Projet", result.get().getTitle());
    }

    @Test
    void testGetProjetByIdNotFound() {
        when(projetRepository.findById("999")).thenReturn(Optional.empty());

        Optional<Projet> result = projetService.getProjetById("999");

        assertFalse(result.isPresent());
    }

    @Test
    void testCreateProjet() {
        Projet projet = new Projet();
        projet.setTitle("Nouveau Projet");
        projet.setStatus("open");

        when(projetRepository.save(projet)).thenReturn(projet);

        Projet result = projetService.createProjet(projet);

        assertNotNull(result);
        assertEquals("Nouveau Projet", result.getTitle());
        verify(projetRepository, times(1)).save(projet);
    }

    @Test
    void testDeleteProjet() {
        doNothing().when(projetRepository).deleteById("1");

        projetService.deleteProjet("1");

        verify(projetRepository, times(1)).deleteById("1");
    }
}
