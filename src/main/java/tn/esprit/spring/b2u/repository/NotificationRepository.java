package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.spring.b2u.entity.Notification;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);
}
