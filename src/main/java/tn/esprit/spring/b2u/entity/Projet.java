package tn.esprit.spring.b2u.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Projet {

    @Id
    private String id;

    private String nomProjet;
    private String descriptionProjet;
    private String delaiEstime;
    private Double prixProjet;
    private String idEquipeAssocie;
}