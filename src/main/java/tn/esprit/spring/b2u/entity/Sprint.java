package tn.esprit.spring.b2u.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "sprints")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sprint {

    @Id
    private String id;

    private String projetId;
    private String name;
    private int number;
    private Date startDate;
    private Date endDate;
    private String status; // "planned", "active", "completed"
    private List<String> taskIds;
}