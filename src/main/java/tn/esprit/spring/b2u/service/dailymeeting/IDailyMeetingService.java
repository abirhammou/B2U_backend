package tn.esprit.spring.b2u.service.dailymeeting;

import tn.esprit.spring.b2u.entity.DailyMeeting;
import java.util.List;
import java.util.Optional;

public interface IDailyMeetingService {
    List<DailyMeeting> getAllMeetings();
    Optional<DailyMeeting> getMeetingById(String id);
    List<DailyMeeting> getMeetingsByProjet(String projetId);
    List<DailyMeeting> getMeetingsByEquipe(String equipeId);
    DailyMeeting createMeeting(DailyMeeting meeting);
    DailyMeeting updateMeeting(String id, DailyMeeting meeting);
    void deleteMeeting(String id);
    DailyMeeting generateSummary(String id);
}
