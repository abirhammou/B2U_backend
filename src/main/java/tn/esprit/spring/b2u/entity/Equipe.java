package tn.esprit.spring.b2u.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

// ✅ REMOVED the incorrect import:
// import org.springframework.data.mongodb.core.messaging.Task;

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

    List<Task> tasks = new ArrayList<>();  // ✅ Now this uses YOUR Task class

    @Field("entreprise_id")
    String entrepriseId;

    @Field("jira_project_key")
    String jiraProjectKey;
}