package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.Attendance;
import tn.esprit.spring.b2u.service.attendance.IAttendanceService;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final IAttendanceService attendanceService;

    // ✔ marquer présence
    @PostMapping("/mark")
    public Attendance markAttendance(@RequestBody Attendance attendance) {
        return attendanceService.markAttendance(attendance);
    }

    // ✔ récupérer par étudiant
    @GetMapping("/student/{studentId}")
    public List<Attendance> getByStudent(@PathVariable String studentId) {
        return attendanceService.getByStudent(studentId);
    }

    // ✔ nombre d'absences
    @GetMapping("/absences/{studentId}")
    public long countAbsences(@PathVariable String studentId) {
        return attendanceService.countAbsences(studentId);
    }

    // ✔ taux de présence
    @GetMapping("/presence-rate/{studentId}")
    public double getPresenceRate(@PathVariable String studentId) {
        return attendanceService.getPresenceRate(studentId);
    }

    // ✔ étudiants sérieux par entreprise
    @GetMapping("/good-students/{entrepriseId}")
    public List<String> getGoodStudents(@PathVariable String entrepriseId) {
        return attendanceService.getGoodStudents(entrepriseId);
    }
}
