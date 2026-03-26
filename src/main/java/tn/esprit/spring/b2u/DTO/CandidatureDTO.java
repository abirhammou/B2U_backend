package tn.esprit.spring.b2u.DTO;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public class CandidatureDTO {

    private String idCandidature;

    @NotBlank(message = "Le nom du candidat est obligatoire")
    private String nomCandidat;

    @NotBlank(message = "Le prénom du candidat est obligatoire")
    private String prenomCandidat;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;

    private String adresse;

    @Past(message = "La date de naissance doit être passée")
    private LocalDate dateNaissance;

    private String formationActuelle;
    private String specialite;

    @Min(value = 0, message = "Les années d'expérience doivent être positives")
    private int anneeExperience;

    @PastOrPresent(message = "La date de candidature ne peut pas être dans le futur")
    private LocalDate dateCandidature;

    @NotBlank(message = "Le statut est obligatoire")
    private String statutCandidature;

    private List<String> competences;
    private String cvLien;
    private String lettreMotivation;

    // Constructeur vide
    public CandidatureDTO() {}

    // Getters et Setters
    public String getIdCandidature() { return idCandidature; }
    public void setIdCandidature(String idCandidature) { this.idCandidature = idCandidature; }

    public String getNomCandidat() { return nomCandidat; }
    public void setNomCandidat(String nomCandidat) { this.nomCandidat = nomCandidat; }

    public String getPrenomCandidat() { return prenomCandidat; }
    public void setPrenomCandidat(String prenomCandidat) { this.prenomCandidat = prenomCandidat; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getFormationActuelle() { return formationActuelle; }
    public void setFormationActuelle(String formationActuelle) { this.formationActuelle = formationActuelle; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public int getAnneeExperience() { return anneeExperience; }
    public void setAnneeExperience(int anneeExperience) { this.anneeExperience = anneeExperience; }

    public LocalDate getDateCandidature() { return dateCandidature; }
    public void setDateCandidature(LocalDate dateCandidature) { this.dateCandidature = dateCandidature; }

    public String getStatutCandidature() { return statutCandidature; }
    public void setStatutCandidature(String statutCandidature) { this.statutCandidature = statutCandidature; }

    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences; }

    public String getCvLien() { return cvLien; }
    public void setCvLien(String cvLien) { this.cvLien = cvLien; }

    public String getLettreMotivation() { return lettreMotivation; }
    public void setLettreMotivation(String lettreMotivation) { this.lettreMotivation = lettreMotivation; }
}