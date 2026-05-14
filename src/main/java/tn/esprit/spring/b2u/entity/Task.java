package tn.esprit.spring.b2u.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {
    private String title;      // kept as "title" (your existing field)
    private String status;     // TODO | IN_PROGRESS | DONE
    private String assignedTo; // student ID assigned by company
    private String description;
}