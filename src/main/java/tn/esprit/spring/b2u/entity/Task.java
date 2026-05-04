package tn.esprit.spring.b2u.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {

    @Id
    private String id;

    private String projetId;
    private String sprintId;
    private String title;
    private String description;
    private String status; // "todo", "in-progress", "done"
    private String assignedTo;
    private int priority; // 1=low, 2=medium, 3=high
}