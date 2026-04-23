package tn.esprit.spring.b2u.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "work_posts")
public class WorkPost {

    @Id
    private String id;

    private String entrepriseId;

    private String title;
    private String description;

    private int hoursPerWeek; // combien l'étudiant travaille
    private int durationWeeks; // durée de la mission

    private String requiredSkills; // simple string pour l'instant
}
