package tn.esprit.spring.b2u.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Data
@Document(collection = "attendance")
public class Attendance {

    @Id
    private String id;

    private String entrepriseId;
    private String studentId;

    private LocalDate date;

    private boolean present; // true = présent, false = absent
}
