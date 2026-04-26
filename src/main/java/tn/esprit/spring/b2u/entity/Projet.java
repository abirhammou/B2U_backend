package tn.esprit.spring.b2u.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "projets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Projet {

    @Id
    private String id;

    private String title;
    private String description;
    private String companyId;
    private String companyName;
    private List<String> technologies;  // ← Changé de requiredSkills à technologies
    private List<String> requiredSkills; // Gardé pour compatibilité    private int teamSize;
    private Date deadline;
    private String status;
    private int applicantsCount;
    private Date createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getAllRequiredSkills() {
        if (technologies != null && !technologies.isEmpty()) {
            return technologies;
        }
        return requiredSkills != null ? requiredSkills : List.of();
    }
}