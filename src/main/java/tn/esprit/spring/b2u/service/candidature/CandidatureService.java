package tn.esprit.spring.b2u.service.candidature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.DTO.MatchingResult;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.service.CvAnalysisService;
import tn.esprit.spring.b2u.service.InterviewQuestionAiService;
import tn.esprit.spring.b2u.service.notification.StudentNotificationService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired
    private CandidatureRepo candidatureRepository;

    @Autowired
    private ProjetRepository projectRepository;

    @Autowired
    private CvAnalysisService cvAnalysisService;

    @Autowired
    private InterviewQuestionAiService interviewQuestionAiService;

    @Autowired
    private StudentNotificationService studentNotificationService;

    public CandidatureDTO createCandidature(CandidatureDTO dto,
                                            byte[] cvBytes,
                                            byte[] lettreBytes,
                                            String projectId) {

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            new File(uploadDir).mkdirs();

            String cvFileName = System.currentTimeMillis() + "_cv.pdf";
            String lettreFileName = System.currentTimeMillis() + "_lettre.pdf";

            try (FileOutputStream cvOut = new FileOutputStream(uploadDir + cvFileName);
                 FileOutputStream lettreOut = new FileOutputStream(uploadDir + lettreFileName)) {
                cvOut.write(cvBytes);
                lettreOut.write(lettreBytes);
            }

            List<String> extractedSkills = cvAnalysisService.extractSkillsFromBytes(cvBytes);
            if (dto.getCompetences() == null || dto.getCompetences().isEmpty()) {
                dto.setCompetences(extractedSkills);
            }

            Projet project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Projet non trouve avec l'ID: " + projectId));

            Candidature candidature = buildCandidatureInProgress(dto, project, cvFileName, lettreFileName);
            Candidature savedInProgress = candidatureRepository.save(candidature);

            MatchingResult matchingResult = calculerMatchingAvance(
                    dto.getCompetences(),
                    project.getAllRequiredSkills(),
                    dto.getAnneeExperience(),
                    project.getType()
            );

            savedInProgress.setStatutCandidature("EN_REVUE");
            savedInProgress.setScoreMatching(matchingResult.getScore());
            savedInProgress.setRecommendation(matchingResult.getRecommendation());
            savedInProgress.setMatchingDetails(buildMatchingDetails(matchingResult));

            Candidature saved = candidatureRepository.save(savedInProgress);
            project.setApplicantsCount((int) candidatureRepository.countByProjectId(project.getId()));
            projectRepository.save(project);

            afficherRecommandationEntreprise(saved, project, matchingResult);

            return convertToDTO(saved);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    private Candidature buildCandidatureInProgress(CandidatureDTO dto,
                                                   Projet project,
                                                   String cvFileName,
                                                   String lettreFileName) {
        Candidature c = new Candidature();
        c.setNomCandidat(dto.getNomCandidat());
        c.setPrenomCandidat(dto.getPrenomCandidat());
        c.setEmail(dto.getEmail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        c.setFormationActuelle(dto.getFormationActuelle());
        c.setSpecialite(dto.getSpecialite());
        c.setAnneeExperience(dto.getAnneeExperience());
        c.setDateCandidature(LocalDate.now());
        c.setStatutCandidature("EN_COURS");
        c.setProjectId(project.getId());
        c.setProjectTitle(project.getTitle());
        c.setProjectType(project.getType());
        c.setCompanyId(project.getCompanyId());
        c.setCompanyName(project.getCompanyName());
        c.setCompetences(dto.getCompetences());
        c.setCvLien("uploads/" + cvFileName);
        c.setLettreMotivation("uploads/" + lettreFileName);
        return c;
    }

    private MatchingResult calculerMatchingAvance(List<String> candidatSkills,
                                                  List<String> projetTechs,
                                                  int anneeExperience,
                                                  String projectType) {
        MatchingResult result = new MatchingResult();

        if (candidatSkills == null) {
            candidatSkills = new ArrayList<>();
        }

        if (projetTechs == null || projetTechs.isEmpty()) {
            result.setScore(0);
            result.setRecommendation("Aucune technologie specifiee pour ce projet.");
            result.setLevel("NON_DEFINI");
            result.setMatchedSkills(new ArrayList<>());
            result.setPartialSkills(new ArrayList<>());
            result.setMissingSkills(new ArrayList<>());
            result.setSkillScores(new HashMap<>());
            result.setSummary("Projet sans technologies definies.");
            return result;
        }

        List<String> candidatSkillsNorm = candidatSkills.stream()
                .filter(skill -> skill != null && !skill.isBlank())
                .map(skill -> skill.toLowerCase().trim())
                .collect(Collectors.toList());

        List<String> requiredTechs = projetTechs.stream()
                .filter(tech -> tech != null && !tech.isBlank())
                .collect(Collectors.toList());
        if (requiredTechs.isEmpty()) {
            result.setScore(0);
            result.setRecommendation("Aucune technologie exploitable pour ce projet.");
            result.setLevel("NON_DEFINI");
            result.setMatchedSkills(new ArrayList<>());
            result.setPartialSkills(new ArrayList<>());
            result.setMissingSkills(new ArrayList<>());
            result.setSkillScores(new HashMap<>());
            result.setSummary("Projet sans technologies exploitables.");
            return result;
        }

        List<String> projetTechsNorm = requiredTechs.stream()
                .map(tech -> tech.toLowerCase().trim())
                .collect(Collectors.toList());

        List<String> matchedSkills = new ArrayList<>();
        List<String> partialSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        Map<String, Integer> skillScores = new HashMap<>();

        for (int i = 0; i < projetTechsNorm.size(); i++) {
            String requiredTech = projetTechsNorm.get(i);
            String displayTech = requiredTechs.get(i);
            int bestMatchScore = 0;
            String bestCandidateSkill = "";

            for (String candidateSkill : candidatSkillsNorm) {
                int candidateScore = scoreSkill(candidateSkill, requiredTech);
                if (candidateScore > bestMatchScore) {
                    bestMatchScore = candidateScore;
                    bestCandidateSkill = candidateSkill;
                }
                if (bestMatchScore == 100) {
                    break;
                }
            }

            skillScores.put(requiredTech, bestMatchScore);
            if (bestMatchScore >= 70) {
                matchedSkills.add(displayTech);
            } else if (bestMatchScore > 0) {
                partialSkills.add(formatPartialSkill(bestCandidateSkill, displayTech));
            } else {
                missingSkills.add(displayTech);
            }
        }

        int baseScore = (int) Math.round(
                skillScores.values().stream().mapToInt(Integer::intValue).average().orElse(0)
        );
        int typeBonus = calculateProjectTypeBonus(candidatSkillsNorm, anneeExperience, projectType);
        int finalScore = Math.min(100, Math.max(0, baseScore + typeBonus));

        String level = determinerNiveau(finalScore);
        String recommendation = genererRecommandation(
                finalScore,
                matchedSkills.size(),
                partialSkills.size(),
                missingSkills,
                anneeExperience,
                projetTechsNorm.size(),
                projectType,
                typeBonus
        );
        String summary = String.format(
                "Profil %s avec %d competence(s) compatible(s), %d partielle(s), %d manquante(s). Type evalue: %s. Bonus type: %d. Experience: %d an(s). Score final: %d%%",
                level, matchedSkills.size(), partialSkills.size(), missingSkills.size(), normalizeProjectType(projectType), typeBonus, anneeExperience, finalScore
        );

        result.setScore(finalScore);
        result.setRecommendation(recommendation);
        result.setLevel(level);
        result.setMatchedSkills(matchedSkills);
        result.setPartialSkills(partialSkills);
        result.setMissingSkills(missingSkills);
        result.setSkillScores(skillScores);
        result.setSummary(summary);

        return result;
    }

    private int scoreSkill(String candidateSkill, String requiredTech) {
        if (candidateSkill.equals(requiredTech)) {
            return 100;
        }
        if (candidateSkill.contains(requiredTech) || requiredTech.contains(candidateSkill)) {
            return 70;
        }
        if (isSimilarSkill(candidateSkill, requiredTech)) {
            return 50;
        }
        if (isPartialAlternative(candidateSkill, requiredTech)) {
            return 40;
        }
        return 0;
    }

    private boolean isSimilarSkill(String skill1, String skill2) {
        Map<String, List<String>> similarSkills = new HashMap<>();
        similarSkills.put("javascript", Arrays.asList("js", "ecmascript"));
        similarSkills.put("typescript", Arrays.asList("ts"));
        similarSkills.put("spring", Arrays.asList("spring boot", "spring framework"));
        similarSkills.put("react", Arrays.asList("reactjs", "react.js"));
        similarSkills.put("angular", Arrays.asList("angularjs", "angular.js"));
        similarSkills.put("node", Arrays.asList("nodejs", "node.js"));
        similarSkills.put("python", Arrays.asList("py", "python3"));
        similarSkills.put("c++", Arrays.asList("cpp", "cplusplus"));
        similarSkills.put("c#", Arrays.asList("csharp", "dotnet"));

        for (Map.Entry<String, List<String>> entry : similarSkills.entrySet()) {
            String main = entry.getKey();
            List<String> aliases = entry.getValue();
            if ((skill1.equals(main) && aliases.contains(skill2)) ||
                    (skill2.equals(main) && aliases.contains(skill1))) {
                return true;
            }
        }
        return false;
    }

    private boolean isPartialAlternative(String skill1, String skill2) {
        List<List<String>> families = Arrays.asList(
                Arrays.asList("angular", "angularjs", "angular.js", "react", "reactjs", "react.js", "vue", "vue.js", "svelte"),
                Arrays.asList("spring boot", "spring", "jakarta ee", "java ee", "quarkus", "micronaut"),
                Arrays.asList("docker", "kubernetes", "podman", "containerd"),
                Arrays.asList("mysql", "postgresql", "mongodb", "oracle", "sql server", "mariadb"),
                Arrays.asList("aws", "azure", "gcp", "google cloud", "amazon web services")
        );

        return families.stream().anyMatch(family -> family.contains(skill1) && family.contains(skill2));
    }

    private String formatPartialSkill(String candidatSkill, String requiredSkill) {
        if (candidatSkill == null || candidatSkill.isBlank()) {
            return requiredSkill;
        }
        return capitalizeSkillLabel(candidatSkill) + " -> " + requiredSkill;
    }

    private String capitalizeSkillLabel(String skill) {
        return Arrays.stream(skill.trim().split("\\s+"))
                .map(word -> word.isEmpty()
                        ? word
                        : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    private String determinerNiveau(int score) {
        if (score >= 80) return "EXCELLENT";
        if (score >= 65) return "TRES_BON";
        if (score >= 50) return "MOYEN";
        if (score >= 35) return "PARTIEL";
        return "FAIBLE";
    }

    private int calculateProjectTypeBonus(List<String> candidatSkillsNorm, int experience, String projectType) {
        String type = normalizeProjectType(projectType);
        if (type.equals("STAGE")) {
            if (experience <= 1) return 8;
            if (experience <= 2) return 5;
            return 2;
        }
        if (type.equals("HACKATHON")) {
            int bonus = 0;
            if (hasAnySkill(candidatSkillsNorm, "hackathon", "prototype", "mvp", "teamwork", "agile", "scrum")) {
                bonus += 8;
            }
            if (hasAnySkill(candidatSkillsNorm, "github", "git", "api", "ui", "ux")) {
                bonus += 4;
            }
            return Math.min(12, bonus);
        }
        if (experience >= 2) return 6;
        if (experience >= 1) return 3;
        return 0;
    }

    private boolean hasAnySkill(List<String> skills, String... expected) {
        if (skills == null || skills.isEmpty()) {
            return false;
        }
        return Arrays.stream(expected)
                .anyMatch(keyword -> skills.stream().anyMatch(skill -> skill.contains(keyword)));
    }

    private String normalizeProjectType(String projectType) {
        String normalized = projectType == null ? "PROJET" : projectType.trim().toUpperCase();
        if (normalized.equals("STAGE") || normalized.equals("HACKATHON")) {
            return normalized;
        }
        return "PROJET";
    }

    private String genererRecommandation(int score,
                                         int matchedCount,
                                         int partialCount,
                                         List<String> missingSkills,
                                         int experience,
                                         int totalRequired,
                                         String projectType,
                                         int typeBonus) {
        String missingPreview = missingSkills == null || missingSkills.isEmpty()
                ? "aucune competence critique"
                : String.join(", ", missingSkills.stream().limit(3).collect(Collectors.toList()));
        String typeContext = String.format(" Critere %s integre au score: %+d point(s).", normalizeProjectType(projectType), typeBonus);

        if (score >= 80) {
            return String.format(
                    "Recommandation forte. Profil tres aligne avec l'offre: %d/%d competences compatibles. Experience: %d an(s). A inviter en entretien.%s",
                    matchedCount, totalRequired, experience, typeContext
            );
        }
        if (score >= 65) {
            return String.format(
                    "Recommandation positive. Bon profil avec %d/%d competences compatibles et %d correspondance(s) partielle(s). Points a verifier: %s.%s",
                    matchedCount, totalRequired, partialCount, missingPreview, typeContext
            );
        }
        if (score >= 50) {
            return String.format(
                    "Candidat moyen. Bon socle technique avec %d/%d competences compatibles et %d correspondance(s) partielle(s), mais il manque: %s.%s",
                    matchedCount, totalRequired, partialCount, missingPreview, typeContext
            );
        }
        if (score >= 35) {
            return String.format(
                    "Recommandation limitee. Profil partiel: %d/%d competences compatibles. Manques principaux: %s.%s",
                    matchedCount, totalRequired, missingPreview, typeContext
            );
        }
        return String.format(
                "Recommandation negative. Profil peu adapte a cette offre: %d/%d competences compatibles. Manques principaux: %s.%s",
                matchedCount, totalRequired, missingPreview, typeContext
        );
    }

    private void afficherRecommandationEntreprise(Candidature candidature,
                                                  Projet projet,
                                                  MatchingResult result) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("RECOMMANDATION POUR L'ENTREPRISE");
        System.out.println("=".repeat(70));
        System.out.println("Projet: " + projet.getTitle());
        System.out.println("Candidat: " + candidature.getPrenomCandidat() + " " + candidature.getNomCandidat());
        System.out.println("Score de matching: " + result.getScore() + "% - " + result.getLevel());
        System.out.println("Recommendation: " + result.getRecommendation());
        System.out.println("Competences compatibles: " + formatSkills(result.getMatchedSkills()));
        System.out.println("Correspondances partielles: " + formatSkills(result.getPartialSkills()));
        System.out.println("Competences manquantes: " + formatSkills(result.getMissingSkills()));
        System.out.println("Statut: " + candidature.getStatutCandidature());
        System.out.println("=".repeat(70) + "\n");
    }

    private String formatSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) return "Aucune";
        return String.join(", ", skills);
    }

    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureRepository.findAll()
                .stream()
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<CandidatureDTO> getPagedCandidatures(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        return candidatureRepository.findAll(PageRequest.of(safePage, safeSize))
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO);
    }

    public CandidatureDTO getCandidatureById(String id) {
        return candidatureRepository.findById(id)
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvee avec l'ID: " + id));
    }

    public List<CandidatureDTO> getCandidaturesByProject(String projectId) {
        Projet project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouve avec l'ID: " + projectId));

        return candidatureRepository.findByProjectId(project.getId())
                .stream()
                .filter(c -> c.getScoreMatching() > 0)
                .sorted((c1, c2) -> Integer.compare(c2.getScoreMatching(), c1.getScoreMatching()))
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CandidatureDTO> getCandidaturesByCompany(String companyId) {
        Map<String, Candidature> byId = new HashMap<>();
        candidatureRepository.findByCompanyId(companyId)
                .forEach(c -> byId.put(c.getIdCandidature(), c));
        candidatureRepository.findByCompanyName(companyId)
                .forEach(c -> byId.put(c.getIdCandidature(), c));

        return byId.values()
                .stream()
                .sorted((c1, c2) -> Integer.compare(c2.getScoreMatching(), c1.getScoreMatching()))
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CandidatureDTO> getTopCandidatesForProject(String projectId, int limit) {
        List<CandidatureDTO> all = getCandidaturesByProject(projectId);
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    public List<CandidatureDTO> getRecommendedCandidates() {
        return candidatureRepository.findAll()
                .stream()
                .filter(c -> c.getScoreMatching() >= 65)
                .sorted((c1, c2) -> Integer.compare(c2.getScoreMatching(), c1.getScoreMatching()))
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getMatchingStats(String projectId) {
        List<CandidatureDTO> candidates = getCandidaturesByProject(projectId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCandidates", candidates.size());
        stats.put("excellentCount", candidates.stream().filter(c -> c.getScoreMatching() >= 80).count());
        stats.put("goodCount", candidates.stream().filter(c -> c.getScoreMatching() >= 65 && c.getScoreMatching() < 80).count());
        stats.put("averageCount", candidates.stream().filter(c -> c.getScoreMatching() >= 50 && c.getScoreMatching() < 65).count());
        stats.put("weakCount", candidates.stream().filter(c -> c.getScoreMatching() < 50).count());
        stats.put("averageScore", candidates.stream().mapToInt(CandidatureDTO::getScoreMatching).average().orElse(0));
        stats.put("topScore", candidates.stream().mapToInt(CandidatureDTO::getScoreMatching).max().orElse(0));

        return stats;
    }

    public List<CandidatureDTO> getCandidaturesByEmail(String email) {
        return candidatureRepository.findByEmail(email)
                .stream()
                .map(this::ensureInterviewPreparation)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CandidatureDTO createCandidature(CandidatureDTO dto) {
        Candidature c = new Candidature();
        c.setNomCandidat(dto.getNomCandidat());
        c.setPrenomCandidat(dto.getPrenomCandidat());
        c.setEmail(dto.getEmail());
        c.setTelephone(dto.getTelephone());
        c.setAdresse(dto.getAdresse());
        c.setFormationActuelle(dto.getFormationActuelle());
        c.setSpecialite(dto.getSpecialite());
        c.setAnneeExperience(dto.getAnneeExperience());
        c.setDateCandidature(dto.getDateCandidature() != null ? dto.getDateCandidature() : LocalDate.now());
        c.setStatutCandidature(dto.getStatutCandidature());
        c.setCompetences(dto.getCompetences());
        c.setProjectId(dto.getProjectId());
        c.setProjectTitle(dto.getProjectTitle());
        c.setProjectType(dto.getProjectType());
        c.setCompanyId(dto.getCompanyId());
        c.setCompanyName(dto.getCompanyName());
        c.setScoreMatching(dto.getScoreMatching());
        c.setRecommendation(dto.getRecommendation());
        c.setMatchingDetails(dto.getMatchingDetails());
        c.setInterviewPreparation(dto.getInterviewPreparation());

        if (dto.getProjectId() != null && !dto.getProjectId().isBlank()) {
            projectRepository.findById(dto.getProjectId()).ifPresent(project -> {
                c.setProjectTitle(project.getTitle());
                c.setProjectType(project.getType());
                c.setCompanyId(project.getCompanyId());
                c.setCompanyName(project.getCompanyName());
            });
        }

        Candidature saved = candidatureRepository.save(c);
        refreshProjectApplicants(saved.getProjectId());

        return convertToDTO(saved);
    }

    public CandidatureDTO updateCandidature(String id, CandidatureDTO dto) {
        Candidature existing = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvee avec l'ID: " + id));

        String previousProjectId = existing.getProjectId();
        String previousStatus = existing.getStatutCandidature();

        existing.setNomCandidat(dto.getNomCandidat());
        existing.setPrenomCandidat(dto.getPrenomCandidat());
        existing.setEmail(dto.getEmail());
        existing.setTelephone(dto.getTelephone());
        existing.setAdresse(dto.getAdresse());
        existing.setFormationActuelle(dto.getFormationActuelle());
        existing.setSpecialite(dto.getSpecialite());
        existing.setAnneeExperience(dto.getAnneeExperience());
        existing.setDateCandidature(dto.getDateCandidature());
        existing.setStatutCandidature(dto.getStatutCandidature());
        existing.setProjectId(dto.getProjectId());
        existing.setProjectTitle(dto.getProjectTitle());
        existing.setProjectType(dto.getProjectType());
        existing.setCompanyId(dto.getCompanyId());
        existing.setCompanyName(dto.getCompanyName());
        existing.setCompetences(dto.getCompetences());
        if (dto.getScoreMatching() > 0 || existing.getScoreMatching() == 0) {
            existing.setScoreMatching(dto.getScoreMatching());
        }
        if (dto.getRecommendation() != null) {
            existing.setRecommendation(dto.getRecommendation());
        }
        if (dto.getMatchingDetails() != null) {
            existing.setMatchingDetails(dto.getMatchingDetails());
        }
        if (dto.getInterviewPreparation() != null) {
            existing.setInterviewPreparation(dto.getInterviewPreparation());
        }

        if (dto.getProjectId() != null && !dto.getProjectId().isBlank()) {
            projectRepository.findById(dto.getProjectId()).ifPresent(project -> {
                existing.setProjectTitle(project.getTitle());
                existing.setProjectType(project.getType());
                existing.setCompanyId(project.getCompanyId());
                existing.setCompanyName(project.getCompanyName());
            });
        }

        if (shouldGenerateInterviewPreparation(existing.getStatutCandidature())
                && (existing.getInterviewPreparation() == null || existing.getInterviewPreparation().isBlank())) {
            existing.setInterviewPreparation(genererPreparationEntretien(existing));
        }

        Candidature saved = candidatureRepository.save(existing);
        refreshProjectApplicants(previousProjectId);
        refreshProjectApplicants(saved.getProjectId());
        notifyStudentIfStatusChanged(saved, previousStatus);

        return convertToDTO(saved);
    }

    public void deleteCandidature(String id) {
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvee avec l'ID: " + id));

        String projectId = candidature.getProjectId();
        candidatureRepository.deleteById(id);
        refreshProjectApplicants(projectId);
    }

    private CandidatureDTO convertToDTO(Candidature c) {
        CandidatureDTO dto = new CandidatureDTO();

        dto.setIdCandidature(c.getIdCandidature());
        dto.setNomCandidat(c.getNomCandidat());
        dto.setPrenomCandidat(c.getPrenomCandidat());
        dto.setEmail(c.getEmail());
        dto.setTelephone(c.getTelephone());
        dto.setAdresse(c.getAdresse());
        dto.setFormationActuelle(c.getFormationActuelle());
        dto.setSpecialite(c.getSpecialite());
        dto.setAnneeExperience(c.getAnneeExperience());
        dto.setDateCandidature(c.getDateCandidature());
        dto.setStatutCandidature(c.getStatutCandidature());
        dto.setProjectId(c.getProjectId());
        dto.setProjectTitle(c.getProjectTitle());
        dto.setProjectType(c.getProjectType());
        dto.setCompanyId(c.getCompanyId());
        dto.setCompanyName(c.getCompanyName());
        dto.setCompetences(c.getCompetences());
        dto.setCvLien(c.getCvLien());
        dto.setLettreMotivation(c.getLettreMotivation());
        dto.setScoreMatching(c.getScoreMatching());
        dto.setRecommendation(c.getRecommendation());
        dto.setMatchingDetails(c.getMatchingDetails());
        dto.setInterviewPreparation(c.getInterviewPreparation());

        if (dto.getProjectId() != null && !dto.getProjectId().isBlank()
                && (dto.getProjectTitle() == null || dto.getProjectTitle().isBlank())) {
            projectRepository.findById(dto.getProjectId()).ifPresent(project -> {
                dto.setProjectTitle(project.getTitle());
                dto.setProjectType(project.getType());
                dto.setCompanyId(project.getCompanyId());
                dto.setCompanyName(project.getCompanyName());
            });
        }

        return dto;
    }

    private String buildMatchingDetails(MatchingResult result) {
        return "Matched skills: " + formatSkills(result.getMatchedSkills())
                + " | Partial skills: " + formatSkills(result.getPartialSkills())
                + " | Missing skills: " + formatSkills(result.getMissingSkills())
                + " | Summary: " + result.getSummary();
    }

    private boolean shouldGenerateInterviewPreparation(String status) {
        if (status == null) {
            return false;
        }
        String normalized = normalizeDecisionStatus(status);
        return normalized.equals("ACCEPTEE")
                || normalized.equals("PRESELECTIONNE")
                || normalized.equals("PRESELECTIONNEE");
    }

    private String normalizeDecisionStatus(String status) {
        return status == null
                ? ""
                : status.trim()
                .toUpperCase()
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace("Ã‰", "E")
                .replace("Ã©", "E")
                .replace(" ", "_");
    }

    private Candidature ensureInterviewPreparation(Candidature candidature) {
        if (candidature != null
                && shouldGenerateInterviewPreparation(candidature.getStatutCandidature())
                && shouldCreateOrRefreshPreparation(candidature.getInterviewPreparation())) {
            candidature.setInterviewPreparation(genererPreparationEntretien(candidature));
            return candidatureRepository.save(candidature);
        }
        return candidature;
    }

    private boolean shouldCreateOrRefreshPreparation(String preparation) {
        return preparation == null
                || preparation.isBlank()
                || !preparation.toLowerCase().contains("reponse modele");
    }

    private void notifyStudentIfStatusChanged(Candidature candidature, String previousStatus) {
        String oldStatus = normalizeDecisionStatus(previousStatus);
        String newStatus = normalizeDecisionStatus(candidature.getStatutCandidature());
        if (!oldStatus.equals(newStatus) && isStudentVisibleDecision(newStatus)) {
            studentNotificationService.notifyStatusChanged(candidature, previousStatus);
        }
    }

    private boolean isStudentVisibleDecision(String status) {
        return status.equals("ACCEPTEE")
                || status.equals("REFUSEE")
                || status.equals("PRESELECTIONNE")
                || status.equals("PRESELECTIONNEE")
                || status.equals("EN_REVUE");
    }

    private String genererPreparationEntretien(Candidature candidature) {
        List<String> skills = candidature.getCompetences() != null ? candidature.getCompetences() : List.of();
        List<String> missingSkills = extractSkillsFromDetails(candidature.getMatchingDetails(), "Missing skills:");
        String mainSkill = skills.isEmpty() ? "vos competences principales" : skills.get(0);
        String missing = missingSkills.isEmpty() ? "les technologies demandees par le projet" : missingSkills.get(0);
        String projectTitle = candidature.getProjectTitle() != null && !candidature.getProjectTitle().isBlank()
                ? candidature.getProjectTitle()
                : "le projet";

        String aiGeneratedPreparation = interviewQuestionAiService.generateInterviewPreparation(candidature, missingSkills);
        if (aiGeneratedPreparation != null && !aiGeneratedPreparation.isBlank()) {
            return aiGeneratedPreparation;
        }

        return String.join("\n",
                "Preparation AI a l'entretien - " + projectTitle,
                "",
                "Questions techniques:",
                "1. Presentez votre experience avec " + mainSkill + " et donnez un exemple concret de projet.",
                "Reponse modele: J'ai utilise " + mainSkill + " dans un projet concret en expliquant le besoin, mon role, les choix techniques et le resultat obtenu.",
                "2. Expliquez comment vous organiseriez votre travail pour contribuer rapidement a " + projectTitle + ".",
                "Reponse modele: Je commence par comprendre les objectifs, lire la documentation, identifier les taches prioritaires et livrer une premiere contribution simple.",
                "3. Decrivez une difficulte technique rencontree et la maniere dont vous l'avez resolue.",
                "Reponse modele: Je presente le probleme, les pistes analysees, la solution choisie et ce que j'ai appris pour eviter le meme blocage.",
                "",
                "Questions sur les points a renforcer:",
                "4. Le matching indique un besoin autour de " + missing + ". Comment comptez-vous vous preparer sur ce point ?",
                "Reponse modele: Je reconnais ce point a renforcer, je prepare un mini-projet ou une formation courte sur " + missing + ", puis je relie cet apprentissage aux besoins du projet.",
                "5. Si vous devez apprendre une nouvelle technologie en peu de temps, quelle methode utilisez-vous ?",
                "Reponse modele: Je lis la documentation officielle, je reproduis un exemple simple, puis je cree une petite fonctionnalite pratique pour valider ma comprehension.",
                "",
                "Questions comportementales:",
                "6. Comment communiquez-vous votre avancement dans une equipe ?",
                "Reponse modele: Je partage ce qui est termine, ce qui est en cours, les blocages et les prochaines etapes avec un contexte clair.",
                "7. Que faites-vous si vous etes bloque pendant un sprint ou une mission ?",
                "Reponse modele: J'isole le probleme, je documente mes essais, puis je sollicite l'equipe avec des informations precises pour avancer rapidement.",
                "",
                "Conseils AI:",
                "- Revisez les competences matchées: " + formatSkills(skills),
                "- Preparez un exemple de projet personnel ou academique lie a cette candidature.",
                "- Preparez une reponse courte sur vos lacunes et votre plan d'apprentissage."
        );
    }

    private List<String> extractSkillsFromDetails(String details, String marker) {
        if (details == null || details.isBlank()) {
            return List.of();
        }
        return Arrays.stream(details.split("\\|"))
                .map(String::trim)
                .filter(part -> part.toLowerCase().startsWith(marker.toLowerCase()))
                .findFirst()
                .map(part -> part.substring(marker.length()).trim())
                .map(part -> Arrays.stream(part.split(","))
                        .map(String::trim)
                        .filter(skill -> !skill.isBlank() && !skill.equalsIgnoreCase("Aucune"))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    private void refreshProjectApplicants(String projectId) {
        if (projectId == null || projectId.isBlank()) {
            return;
        }

        projectRepository.findById(projectId).ifPresent(project -> {
            project.setApplicantsCount((int) candidatureRepository.countByProjectId(projectId));
            projectRepository.save(project);
        });
    }
}
