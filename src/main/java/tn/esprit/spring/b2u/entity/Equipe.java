package tn.esprit.spring.b2u.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "equipes")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Equipe {

    @Id
    String idEquipe;
    String nomMembresEquipe;
    String descriptionProfil;


    String entrepriseId;


    // @JsonIgnore
    // @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL)
    //List<Etudiant> etudiants;



    // @JsonIgnore
    // @OneToOne(mappedBy = "equipeAssocie")
    // Projet projet;
}
