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
    private List<String> requiredSkills;
    private int teamSize;
    private Date deadline;
    private String status;
    private int applicantsCount;
    private Date createdAt;
}