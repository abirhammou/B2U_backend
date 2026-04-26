package tn.esprit.spring.b2u.service.attendance;

import tn.esprit.spring.b2u.entity.Attendance;

import java.util.List;

public interface IAttendanceService {

    Attendance markAttendance(Attendance attendance);

    List<Attendance> getByStudent(String studentId);

    long countAbsences(String studentId);

    double getPresenceRate(String studentId);

    List<String> getGoodStudents(String entrepriseId);
}
