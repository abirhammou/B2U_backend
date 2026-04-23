package tn.esprit.spring.b2u.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
public class EquipeDTO {

    @NotBlank(message = "Le nom des membres est obligatoire")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères")
    private String nomMembresEquipe;

    @NotBlank(message = "La description est obligatoire")
    @Size(min = 5, max = 200, message = "La description doit contenir entre 5 et 200 caractères")
    private String descriptionProfil;

    public EquipeDTO() {}

    public String getNomMembresEquipe() {
        return nomMembresEquipe;
    }

    public void setNomMembresEquipe(String nomMembresEquipe) {
        this.nomMembresEquipe = nomMembresEquipe;
    }

    public String getDescriptionProfil() {
        return descriptionProfil;
    }

    public void setDescriptionProfil(String descriptionProfil) {
        this.descriptionProfil = descriptionProfil;
    }
}
