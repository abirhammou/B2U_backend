package tn.esprit.spring.b2u.service.candidature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.CandidatureDTO;
import tn.esprit.spring.b2u.DTO.MatchingResult;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.CandidatureRepo;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.service.CvAnalysisService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CandidatureService {

    @Autowired
    private CandidatureRepo candidatureRepository;

    @Autowired
    private ProjetRepository projectRepository;

    @Autowired
    private CvAnalysisService cvAnalysisService;

    // ===== CREATE with byte arrays et matching avancé =====
    public CandidatureDTO createCandidature(CandidatureDTO dto,
                                            byte[] cvBytes,
                                            byte[] lettreBytes,
                                            String projectId) {

        try {
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            new File(uploadDir).mkdirs();

            String cvFileName = System.currentTimeMillis() + "_cv.pdf";
            String lettreFileName = System.currentTimeMillis() + "_lettre.pdf";

            // Save files from bytes
            try (FileOutputStream cvOut = new FileOutputStream(uploadDir + cvFileName);
                 FileOutputStream lettreOut = new FileOutputStream(uploadDir + lettreFileName)) {
                cvOut.write(cvBytes);
                lettreOut.write(lettreBytes);
            }

            // 🔥 CV ANALYSIS - Extraire les compétences
            List<String> extractedSkills = cvAnalysisService.extractSkillsFromBytes(cvBytes);
            System.out.println("📋 Compétences extraites du CV: " + extractedSkills);

            if (dto.getCompetences() == null || dto.getCompetences().isEmpty()) {
                dto.setCompetences(extractedSkills);
            }

            // 📌 Récupérer le projet
            Projet project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé avec l'ID: " + projectId));

            System.out.println("🎯 Projet: " + project.getTitle());
            System.out.println("🔧 Technologies requises: " + project.getTechnologies());

            // 🎯 ALGORITHME DE MATCHING AVANCÉ
            MatchingResult matchingResult = calculerMatchingAvance(
                    dto.getCompetences(),
                    project.getTechnologies(),
                    dto.getAnneeExperience()
            );

            int score = matchingResult.getScore();
            String recommandation = matchingResult.getRecommendation();

            System.out.println("📊 Score de matching: " + score + "%");
            System.out.println("💡 Recommandation: " + recommandation);
            System.out.println("✅ Compétences matchées: " + matchingResult.getMatchedSkills());
            System.out.println("❌ Compétences manquantes: " + matchingResult.getMissingSkills());

            // 🧱 Création de l'entité Candidature
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

            // Déterminer le statut basé sur le score
            String statut = determinerStatut(score, matchingResult);
            c.setStatutCandidature(statut);

            c.setCompetences(dto.getCompetences());
            c.setCvLien("uploads/" + cvFileName);
            c.setLettreMotivation("uploads/" + lettreFileName);
            c.setScoreMatching(score);

            Candidature saved = candidatureRepository.save(c);

            // Afficher un résumé pour l'entreprise
            afficherRecommandationEntreprise(saved, project, matchingResult);

            return convertToDTO(saved);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    // ===== ALGORITHME DE MATCHING AVANCÉ =====
    private MatchingResult calculerMatchingAvance(List<String> candidatSkills,
                                                  List<String> projetTechs,
                                                  int anneeExperience) {

        MatchingResult result = new MatchingResult();

        if (candidatSkills == null) candidatSkills = new ArrayList<>();

        if (projetTechs == null || projetTechs.isEmpty()) {
            result.setScore(0);
            result.setRecommendation("⚠️ Aucune technologie spécifiée pour ce projet");
            result.setLevel("NON_DEFINI");
            result.setMatchedSkills(new ArrayList<>());
            result.setMissingSkills(new ArrayList<>());
            result.setSkillScores(new HashMap<>());
            result.setSummary("Projet sans technologies définies");
            return result;
        }

        // Normaliser les compétences (lowercase)
        List<String> candidatSkillsNorm = candidatSkills.stream()
                .map(s -> s.toLowerCase().trim())
                .collect(Collectors.toList());

        List<String> projetTechsNorm = projetTechs.stream()
                .map(t -> t.toLowerCase().trim())
                .collect(Collectors.toList());

        // 1. Calculer les compétences matchées et manquantes
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        Map<String, Integer> skillScores = new HashMap<>();

        for (String tech : projetTechsNorm) {
            boolean found = false;
            int bestMatchScore = 0;
            String matchedSkill = "";

            for (String skill : candidatSkillsNorm) {
                // Matching exact
                if (skill.equals(tech)) {
                    found = true;
                    bestMatchScore = 100;
                    matchedSkill = skill;
                    break;
                }
                // Matching partiel (ex: "spring" dans "spring boot")
                else if (skill.contains(tech) || tech.contains(skill)) {
                    found = true;
                    bestMatchScore = Math.max(bestMatchScore, 70);
                    matchedSkill = skill;
                }
                // Matching similaire (ex: "javascript" et "js")
                else if (isSimilarSkill(skill, tech)) {
                    found = true;
                    bestMatchScore = Math.max(bestMatchScore, 50);
                    matchedSkill = skill;
                }
            }

            if (found) {
                matchedSkills.add(tech);
                skillScores.put(tech, bestMatchScore);
            } else {
                missingSkills.add(tech);
                skillScores.put(tech, 0);
            }
        }

        // 2. Calculer le score de base (pondération des compétences)
        double techScore = (double) matchedSkills.size() / projetTechsNorm.size() * 100;

        // 3. Bonus pour l'expérience
        double experienceBonus = 0;
        if (anneeExperience >= 5) {
            experienceBonus = 15;
        } else if (anneeExperience >= 3) {
            experienceBonus = 10;
        } else if (anneeExperience >= 1) {
            experienceBonus = 5;
        }

        // 4. Score final (capé à 100)
        int finalScore = (int) Math.min(100, techScore + experienceBonus);

        // 5. Déterminer le niveau
        String level;
        if (finalScore >= 80) level = "EXCELLENT";
        else if (finalScore >= 65) level = "TRES_BON";
        else if (finalScore >= 50) level = "BON";
        else if (finalScore >= 35) level = "MOYEN";
        else level = "FAIBLE";

        // 6. Générer une recommandation intelligente
        String recommendation = genererRecommandation(
                finalScore, level, matchedSkills.size(),
                missingSkills.size(), anneeExperience, projetTechsNorm.size()
        );

        // 7. Créer le résumé
        String summary = String.format(
                "Profil %s avec %d/%d compétences matchées. Expérience: %d ans. Score final: %d%%",
                level, matchedSkills.size(), projetTechsNorm.size(), anneeExperience, finalScore
        );

        result.setScore(finalScore);
        result.setRecommendation(recommendation);
        result.setLevel(level);
        result.setMatchedSkills(matchedSkills);
        result.setMissingSkills(missingSkills);
        result.setSkillScores(skillScores);
        result.setSummary(summary);

        return result;
    }

    // Vérifier si deux compétences sont similaires
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

    // Générer une recommandation personnalisée
    private String genererRecommandation(int score, String level,
                                         int matchedCount, int missingCount,
                                         int experience, int totalRequired) {

        if (score >= 80) {
            return String.format(
                    "✅ RECOMMANDATION FORTE : Ce candidat correspond parfaitement au profil recherché. " +
                            "Compétences techniques excellentes (%d/%d compétences matchées). " +
                            "Expérience de %d ans pertinente. À inviter d'urgence en entretien.",
                    matchedCount, totalRequired, experience
            );
        }
        else if (score >= 65) {
            return String.format(
                    "👍 RECOMMANDATION POSITIVE : Très bon profil. " +
                            "Le candidat possède la majorité des compétences requises (%d/%d). " +
                            "Compétences manquantes: %s. À contacter pour un entretien.",
                    matchedCount, totalRequired, getMissingSkillsPreview(missingCount)
            );
        }
        else if (score >= 50) {
            return String.format(
                    "📋 RECOMMANDATION CONDITIONNELLE : Profil intéressant mais avec des lacunes. " +
                            "Le candidat possède %d/%d compétences clés. " +
                            "%s. À considérer si aucun meilleur profil n'est disponible.",
                    matchedCount, totalRequired,
                    experience >= 3 ?
                            "Son expérience de " + experience + " ans pourrait compenser les lacunes techniques" :
                            "Pourrait convenir pour un poste junior ou en formation"
            );
        }
        else if (score >= 35) {
            return String.format(
                    "⚠️ RECOMMANDATION LIMITÉE : Profil ne correspondant que partiellement. " +
                            "Seulement %d/%d compétences matchées. " +
                            "À garder en réserve pour d'autres opportunités.",
                    matchedCount, totalRequired
            );
        }
        else {
            return String.format(
                    "❌ RECOMMANDATION NÉGATIVE : Profil non adapté à ce projet. " +
                            "Trop peu de compétences matchées (%d/%d). Ne pas retenir pour ce poste.",
                    matchedCount, totalRequired
            );
        }
    }

    // Helper pour avoir un aperçu des compétences manquantes
    private String getMissingSkillsPreview(int missingCount) {
        if (missingCount <= 2) return "quelques compétences secondaires";
        if (missingCount <= 4) return "plusieurs compétences techniques";
        return "compétences techniques importantes";
    }

    // Déterminer le statut basé sur le score
    private String determinerStatut(int score, MatchingResult result) {
        if (score >= 80) return "RECOMMANDE";
        if (score >= 65) return "PRESELECTIONNE";
        if (score >= 50) return "EN_ATTENTE";
        return "NON_RETENU";
    }

    // Afficher la recommandation pour l'entreprise
    private void afficherRecommandationEntreprise(Candidature candidature,
                                                  Projet projet,
                                                  MatchingResult result) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("🏢 RECOMMANDATION POUR L'ENTREPRISE");
        System.out.println("=".repeat(70));
        System.out.println("📌 Projet: " + projet.getTitle());
        System.out.println("👤 Candidat: " + candidature.getPrenomCandidat() + " " + candidature.getNomCandidat());
        System.out.println("📧 Email: " + candidature.getEmail());
        System.out.println("📊 Score de matching: " + result.getScore() + "% - " + result.getLevel());
        System.out.println("\n💡 " + result.getRecommendation());
        System.out.println("\n📈 Détails du matching:");
        System.out.println("   ✅ Compétences matchées (" + result.getMatchedSkills().size() + "): " + formatSkills(result.getMatchedSkills()));
        System.out.println("   ❌ Compétences manquantes (" + result.getMissingSkills().size() + "): " + formatSkills(result.getMissingSkills()));
        System.out.println("   🎯 Expérience: " + candidature.getAnneeExperience() + " ans");
        System.out.println("   📝 Statut: " + candidature.getStatutCandidature());
        System.out.println("=".repeat(70) + "\n");
    }

    // Formater la liste des compétences pour l'affichage
    private String formatSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) return "Aucune";
        return String.join(", ", skills);
    }

    // ===== GET ALL CANDIDATURES =====
    public List<CandidatureDTO> getAllCandidatures() {
        return candidatureRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== GET CANDIDATURES PAR PROJET AVEC SCORES TRIÉS =====
    public List<CandidatureDTO> getCandidaturesByProject(String projectId) {
        Projet project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé avec l'ID: " + projectId));

        return candidatureRepository.findAll()
                .stream()
                .filter(c -> c.getScoreMatching() > 0)
                .sorted((c1, c2) -> Integer.compare(c2.getScoreMatching(), c1.getScoreMatching()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== GET TOP CANDIDATS POUR UN PROJET =====
    public List<CandidatureDTO> getTopCandidatesForProject(String projectId, int limit) {
        List<CandidatureDTO> all = getCandidaturesByProject(projectId);
        return all.stream().limit(limit).collect(Collectors.toList());
    }

    // ===== GET CANDIDATURES RECOMMANDÉES (score >= 65) =====
    public List<CandidatureDTO> getRecommendedCandidates() {
        return candidatureRepository.findAll()
                .stream()
                .filter(c -> c.getScoreMatching() >= 65)
                .sorted((c1, c2) -> Integer.compare(c2.getScoreMatching(), c1.getScoreMatching()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== GET STATISTIQUES DE MATCHING =====
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

    // ===== GET BY EMAIL =====
    public List<CandidatureDTO> getCandidaturesByEmail(String email) {
        return candidatureRepository.findByEmail(email)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ===== UPDATE =====
    public CandidatureDTO updateCandidature(String id, CandidatureDTO dto) {

        Candidature existing = candidatureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + id));

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
        existing.setCompetences(dto.getCompetences());

        return convertToDTO(candidatureRepository.save(existing));
    }

    // ===== DELETE =====
    public void deleteCandidature(String id) {
        if (!candidatureRepository.existsById(id)) {
            throw new ResourceNotFoundException("Candidature non trouvée avec l'ID: " + id);
        }
        candidatureRepository.deleteById(id);
    }

    // ===== DTO CONVERSION =====
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
        dto.setCompetences(c.getCompetences());
        dto.setCvLien(c.getCvLien());
        dto.setLettreMotivation(c.getLettreMotivation());
        dto.setScoreMatching(c.getScoreMatching());

        return dto;
    }
}