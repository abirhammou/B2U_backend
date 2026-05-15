package tn.esprit.spring.b2u.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.StudentProfile;
import tn.esprit.spring.b2u.repository.StudentProfileRepo;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/student-profiles")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Student Profile", description = "Student skills, education, experience")
public class StudentProfileController {

    private final StudentProfileRepo repo;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get student profile by user ID")
    public ResponseEntity<StudentProfile> getByUserId(@PathVariable String userId) {
        return repo.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(
                        StudentProfile.builder()
                                .userId(userId)
                                .education(new java.util.ArrayList<>())
                                .workExperience(new java.util.ArrayList<>())
                                .technicalSkills(new java.util.ArrayList<>())
                                .softSkills(new java.util.ArrayList<>())
                                .languages(new java.util.ArrayList<>())
                                .build()
                ));
    }

    @PostMapping
    @Operation(summary = "Create or update student profile")
    public ResponseEntity<StudentProfile> save(@RequestBody StudentProfile profile) {
        // Generate IDs for sub-items that don't have one
        if (profile.getTechnicalSkills() != null) {
            profile.getTechnicalSkills().forEach(s -> {
                if (s.getId() == null) s.setId(UUID.randomUUID().toString());
            });
        }
        if (profile.getSoftSkills() != null) {
            profile.getSoftSkills().forEach(s -> {
                if (s.getId() == null) s.setId(UUID.randomUUID().toString());
            });
        }
        if (profile.getEducation() != null) {
            profile.getEducation().forEach(e -> {
                if (e.getId() == null) e.setId(UUID.randomUUID().toString());
            });
        }
        if (profile.getWorkExperience() != null) {
            profile.getWorkExperience().forEach(w -> {
                if (w.getId() == null) w.setId(UUID.randomUUID().toString());
            });
        }

        // Upsert — find existing or create
        return repo.findByUserId(profile.getUserId())
                .map(existing -> {
                    profile.setId(existing.getId());
                    profile.setCreatedAt(existing.getCreatedAt());
                    profile.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(repo.save(profile));
                })
                .orElseGet(() -> {
                    profile.setCreatedAt(LocalDateTime.now());
                    profile.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(profile));
                });
    }

    @PutMapping("/user/{userId}")
    @Operation(summary = "Update student profile")
    public ResponseEntity<StudentProfile> update(
            @PathVariable String userId,
            @RequestBody StudentProfile profile) {
        profile.setUserId(userId);
        return ResponseEntity.ok(repo.save(profile));
    }
}