package tn.esprit.spring.b2u.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.service.candidature.CandidatureService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CandidatureControllerFunctionalTest {

    @Mock
    private CandidatureService candidatureService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CandidatureController(candidatureService))
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void createCandidature_retourneLaCandidatureCreee() throws Exception {
        CandidatureDTO request = candidatureDto(null, "student@b2u.tn");
        CandidatureDTO response = candidatureDto("cand-1", "student@b2u.tn");

        when(candidatureService.createCandidature(any(CandidatureDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/candidatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCandidature").value("cand-1"))
                .andExpect(jsonPath("$.email").value("student@b2u.tn"))
                .andExpect(jsonPath("$.projectId").value("project-1"));

        verify(candidatureService).createCandidature(any(CandidatureDTO.class));
    }

    @Test
    void getMesCandidatures_filtreParEmailEtRetourneUneListeJson() throws Exception {
        when(candidatureService.getCandidaturesByEmail("student@b2u.tn"))
                .thenReturn(List.of(candidatureDto("cand-1", "student@b2u.tn")));

        mockMvc.perform(get("/api/candidatures/my")
                        .param("email", "student@b2u.tn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCandidature").value("cand-1"))
                .andExpect(jsonPath("$[0].email").value("student@b2u.tn"));

        verify(candidatureService).getCandidaturesByEmail("student@b2u.tn");
    }

    @Test
    void getStatistiquesMatching_retourneLesIndicateursDuProjet() throws Exception {
        when(candidatureService.getMatchingStats("project-1")).thenReturn(Map.of(
                "totalCandidates", 2,
                "excellentCount", 1L,
                "averageScore", 74.5,
                "topScore", 90
        ));

        mockMvc.perform(get("/api/candidatures/project/project-1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCandidates").value(2))
                .andExpect(jsonPath("$.excellentCount").value(1))
                .andExpect(jsonPath("$.averageScore").value(74.5))
                .andExpect(jsonPath("$.topScore").value(90));

        verify(candidatureService).getMatchingStats("project-1");
    }

    private static CandidatureDTO candidatureDto(String id, String email) {
        CandidatureDTO dto = new CandidatureDTO();
        dto.setIdCandidature(id);
        dto.setNomCandidat("Candidat");
        dto.setPrenomCandidat("Test");
        dto.setEmail(email);
        dto.setTelephone("22111222");
        dto.setAnneeExperience(1);
        dto.setDateCandidature(LocalDate.of(2026, 5, 30));
        dto.setStatutCandidature("En cours");
        dto.setProjectId("project-1");
        dto.setProjectTitle("Plateforme B2U");
        dto.setCompanyId("company-1");
        dto.setCompanyName("B2U Company");
        dto.setScoreMatching(73);
        return dto;
    }
}
