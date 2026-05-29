package tn.esprit.spring.b2u.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "student_profiles")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StudentProfile {

    @Id
    private String id;

    private String userId;  // links to User
    private String bio;
    private String github;
    private String linkedin;
    private String portfolio;

    private List<Education>       education;
    private List<WorkExperience>  workExperience;
    private List<TechnicalSkill>  technicalSkills;
    private List<SoftSkill>       softSkills;
    private List<Language>        languages;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Education {
        private String id;
        private String degree;
        private String field;
        private String institution;
        private int startYear;
        private Integer endYear;
        private boolean current;
        private Double gpa;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class WorkExperience {
        private String id;
        private String title;
        private String company;
        private String type;
        private String startDate;
        private String endDate;
        private boolean current;
        private String description;
        private List<String> technologies;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class TechnicalSkill {
        private String id;
        private String name;
        private String level;   // beginner, intermediate, advanced, expert
        private Integer yearsOfExperience;
        private String category;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SoftSkill {
        private String id;
        private String name;
        private int level;  // 1–5
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Language {
        private String name;
        private String level;
    }
}