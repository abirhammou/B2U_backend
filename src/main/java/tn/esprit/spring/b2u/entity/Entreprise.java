package tn.esprit.spring.b2u.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "enterprises")
public class Entreprise {

    @Id
    private String id;

    private String name;
    private String description;
    private String sector;          // secteur d'activité
    private String address;
    private String email;
    private String phone;

    private String userId;
}
