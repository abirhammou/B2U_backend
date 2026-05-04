package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.DailyMeeting;
import tn.esprit.spring.b2u.service.dailymeeting.DailyMeetingService;

import java.util.List;

@RestController
@RequestMapping("/api/daily-meetings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DailyMeetingController {

    private final DailyMeetingService dailyMeetingService;

    @GetMapping
    public List<DailyMeeting> getAllMeetings() {
        return dailyMeetingService.getAllMeetings();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DailyMeeting> getMeetingById(@PathVariable String id) {
        return dailyMeetingService.getMeetingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/projet/{projetId}")
    public List<DailyMeeting> getMeetingsByProjet(@PathVariable String projetId) {
        return dailyMeetingService.getMeetingsByProjet(projetId);
    }

    @GetMapping("/equipe/{equipeId}")
    public List<DailyMeeting> getMeetingsByEquipe(@PathVariable String equipeId) {
        return dailyMeetingService.getMeetingsByEquipe(equipeId);
    }

    @PostMapping
    public DailyMeeting createMeeting(@RequestBody DailyMeeting meeting) {
        return dailyMeetingService.createMeeting(meeting);
    }

    @PutMapping("/{id}")
    public DailyMeeting updateMeeting(@PathVariable String id, @RequestBody DailyMeeting meeting) {
        return dailyMeetingService.updateMeeting(id, meeting);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeeting(@PathVariable String id) {
        dailyMeetingService.deleteMeeting(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/summary")
    public DailyMeeting generateSummary(@PathVariable String id) {
        return dailyMeetingService.generateSummary(id);
    }
}