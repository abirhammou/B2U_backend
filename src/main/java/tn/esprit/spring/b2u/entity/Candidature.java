package tn.esprit.spring.b2u.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "candidatures")
public class Candidature {

    @Id
    private String idCandidature;

    private String nomCandidat;
    private String prenomCandidat;
    private String email;
    private String telephone;
    private String adresse;


    private String formationActuelle;
    private String specialite;
    private int anneeExperience;

    private LocalDate dateCandidature;
    private String statutCandidature;

    private List<String> competences;
    private String cvLien;
    private String lettreMotivation;

    public Candidature() {}

    // Getters et setters...
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