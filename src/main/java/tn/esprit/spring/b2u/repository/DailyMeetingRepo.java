package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.spring.b2u.entity.DailyMeeting;
import java.util.List;

@Repository
public interface DailyMeetingRepo extends MongoRepository<DailyMeeting, String> {
    List<DailyMeeting> findByProjetId(String projetId);
    List<DailyMeeting> findByEquipeId(String equipeId);
}
