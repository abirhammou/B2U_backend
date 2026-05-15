package tn.esprit.spring.b2u.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "etudiants")
public class Etudiant {

    @Id
    private String id;

    private String userId;      // lien vers User

    private String bio;
    private String phone;
    private String university;
    private String graduationYear;
    private String specialite;
    private String linkedin;
    private String github;
    private List<String> skills;
    private String avatarUrl;
}
