package tn.esprit.spring.b2u.service.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Attendance;
import tn.esprit.spring.b2u.repository.AttendanceRepo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService implements IAttendanceService {

    private final AttendanceRepo attendanceRepo;

    @Override
    public Attendance markAttendance(Attendance attendance) {
        return attendanceRepo.save(attendance);
    }

    @Override
    public List<Attendance> getByStudent(String studentId) {
        return attendanceRepo.findByStudentId(studentId);
    }

    // 🔴 LOGIQUE MÉTIER 1 : nombre d'absences
    @Override
    public long countAbsences(String studentId) {
        return attendanceRepo.findByStudentId(studentId)
                .stream()
                .filter(a -> !a.isPresent())
                .count();
    }

    // 🔴 LOGIQUE MÉTIER 2 : taux de présence
    @Override
    public double getPresenceRate(String studentId) {
        List<Attendance> list = attendanceRepo.findByStudentId(studentId);

        long total = list.size();
        long present = list.stream().filter(Attendance::isPresent).count();

        return total == 0 ? 0 : (present * 100.0) / total;
    }

    // 🔴 LOGIQUE MÉTIER 3 : étudiants sérieux
    @Override
    public List<String> getGoodStudents(String entrepriseId) {

        return attendanceRepo.findByEntrepriseId(entrepriseId)
                .stream()
                .collect(Collectors.groupingBy(
                        Attendance::getStudentId,
                        Collectors.partitioningBy(Attendance::isPresent, Collectors.counting())
                ))
                .entrySet()
                .stream()
                .filter(e -> e.getValue().get(true) > e.getValue().get(false))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
