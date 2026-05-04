package tn.esprit.spring.b2u.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "daily_meetings")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyMeeting {

    @Id
    private String id;

    private String projetId;
    private String equipeId;
    private Date meetingDate;
    private List<DailyResponse> responses;
    private String summary; // résumé généré par IA
}