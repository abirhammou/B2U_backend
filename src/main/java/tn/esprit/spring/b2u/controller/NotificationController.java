package tn.esprit.spring.b2u.controller;

import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.DTO.NotificationDTO;
import tn.esprit.spring.b2u.service.notification.StudentNotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private final StudentNotificationService notificationService;

    public NotificationController(StudentNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/student")
    public List<NotificationDTO> getStudentNotifications(@RequestParam String email) {
        return notificationService.getByStudentEmail(email);
    }

    @PutMapping("/{id}/read")
    public NotificationDTO markAsRead(@PathVariable String id) {
        return notificationService.markAsRead(id);
    }
}
