package tn.esprit.spring.b2u.service.notification;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.NotificationDTO;
import tn.esprit.spring.b2u.entity.Candidature;
import tn.esprit.spring.b2u.entity.Notification;
import tn.esprit.spring.b2u.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentNotificationService {

    private final NotificationRepository notificationRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.from:no-reply@b2u.local}")
    private String mailFrom;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public StudentNotificationService(NotificationRepository notificationRepository,
                                      ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.notificationRepository = notificationRepository;
        this.mailSenderProvider = mailSenderProvider;
    }

    public void notifyStatusChanged(Candidature candidature, String previousStatus) {
        if (candidature == null || candidature.getEmail() == null || candidature.getEmail().isBlank()) {
            return;
        }

        String status = displayStatus(candidature.getStatutCandidature());
        String previous = displayStatus(previousStatus);
        String projectTitle = candidature.getProjectTitle() != null && !candidature.getProjectTitle().isBlank()
                ? candidature.getProjectTitle()
                : "votre candidature";

        Notification notification = new Notification();
        notification.setStudentEmail(candidature.getEmail());
        notification.setCandidatureId(candidature.getIdCandidature());
        notification.setProjectTitle(projectTitle);
        notification.setStatus(status);
        notification.setTitle("Statut de candidature mis a jour");
        notification.setMessage("Votre candidature pour " + projectTitle + " est passee de "
                + previous + " a " + status + ".");
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
        sendStatusEmail(candidature, notification);
    }

    public List<NotificationDTO> getByStudentEmail(String email) {
        return notificationRepository.findByStudentEmailOrderByCreatedAtDesc(email)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public NotificationDTO markAsRead(String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable avec l'ID: " + id));
        notification.setRead(true);
        return toDto(notificationRepository.save(notification));
    }

    private void sendStatusEmail(Candidature candidature, Notification notification) {
        if (!mailEnabled) {
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(candidature.getEmail());
            message.setSubject("Mise a jour de votre candidature B2U");
            message.setText("Bonjour " + safe(candidature.getPrenomCandidat()) + ",\n\n"
                    + notification.getMessage() + "\n\n"
                    + "Connectez-vous a votre espace etudiant pour voir le detail, la recommandation AI et votre preparation d'entretien.\n\n"
                    + "Equipe B2U");
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Email notification non envoye: " + e.getMessage());
        }
    }

    private NotificationDTO toDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setStudentEmail(notification.getStudentEmail());
        dto.setCandidatureId(notification.getCandidatureId());
        dto.setProjectTitle(notification.getProjectTitle());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setStatus(notification.getStatus());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    private String displayStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Inconnu";
        }
        String normalized = status.trim().toUpperCase()
                .replace("É", "E")
                .replace("È", "E")
                .replace("Ê", "E")
                .replace(" ", "_");
        if (normalized.equals("ACCEPTEE")) return "Acceptee";
        if (normalized.equals("REFUSEE")) return "Refusee";
        if (normalized.equals("EN_REVUE")) return "En revue";
        if (normalized.equals("EN_COURS")) return "En cours";
        if (normalized.equals("PRESELECTIONNE") || normalized.equals("PRESELECTIONNEE")) return "Preselectionnee";
        return status;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
