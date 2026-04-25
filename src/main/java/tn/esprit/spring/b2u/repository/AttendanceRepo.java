package tn.esprit.spring.b2u.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.esprit.spring.b2u.entity.Attendance;

import java.util.List;

public interface AttendanceRepo extends MongoRepository<Attendance, String> {

    List<Attendance> findByStudentId(String studentId);

    List<Attendance> findByEntrepriseId(String entrepriseId);
}
