package tn.esprit.spring.b2u.service.dailymeeting;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.DailyMeeting;
import tn.esprit.spring.b2u.entity.DailyResponse;
import tn.esprit.spring.b2u.repository.DailyMeetingRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyMeetingService implements IDailyMeetingService {

    private final DailyMeetingRepo dailyMeetingRepo;

    @Override
    public List<DailyMeeting> getAllMeetings() {
        return dailyMeetingRepo.findAll();
    }

    @Override
    public Optional<DailyMeeting> getMeetingById(String id) {
        return dailyMeetingRepo.findById(id);
    }

    @Override
    public List<DailyMeeting> getMeetingsByProjet(String projetId) {
        return dailyMeetingRepo.findByProjetId(projetId);
    }

    @Override
    public List<DailyMeeting> getMeetingsByEquipe(String equipeId) {
        return dailyMeetingRepo.findByEquipeId(equipeId);
    }

    @Override
    public DailyMeeting createMeeting(DailyMeeting meeting) {
        return dailyMeetingRepo.save(meeting);
    }

    @Override
    public DailyMeeting updateMeeting(String id, DailyMeeting meeting) {
        meeting.setId(id);
        return dailyMeetingRepo.save(meeting);
    }

    @Override
    public void deleteMeeting(String id) {
        dailyMeetingRepo.deleteById(id);
    }

    @Override
    public DailyMeeting generateSummary(String id) {
        DailyMeeting meeting = dailyMeetingRepo.findById(id).orElseThrow();
        List<DailyResponse> responses = meeting.getResponses();

        if (responses == null || responses.isEmpty()) {
            meeting.setSummary("Aucune réponse pour ce daily meeting.");
            return dailyMeetingRepo.save(meeting);
        }

        // Générer un résumé automatique
        StringBuilder summary = new StringBuilder();
        summary.append("📋 Résumé du Daily Meeting\n\n");

        for (DailyResponse response : responses) {
            summary.append("👤 ").append(response.getMemberName()).append("\n");
            summary.append("✅ Hier: ").append(response.getDidYesterday()).append("\n");
            summary.append("🎯 Aujourd'hui: ").append(response.getDoingToday()).append("\n");
            if (response.getBlockers() != null && !response.getBlockers().isEmpty()) {
                summary.append("🚧 Blocages: ").append(response.getBlockers()).append("\n");
            }
            summary.append("\n");
        }

        meeting.setSummary(summary.toString());
        return dailyMeetingRepo.save(meeting);
    }
}
