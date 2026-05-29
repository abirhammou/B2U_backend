package tn.esprit.spring.b2u.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "work_posts")
public class WorkPost {

    @Id
    private String id;

    private String entrepriseId;

    private String title;
    private int hoursPerWeek;

    private String requiredSkills;

    private WorkPostStatus status = WorkPostStatus.ACTIVE;
    private LocalDateTime createdAt;

    private WorkMode workMode;

    private String projetId= null;
    private String projetTitle;

}
