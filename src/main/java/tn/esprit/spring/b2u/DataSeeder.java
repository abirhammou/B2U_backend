package tn.esprit.spring.b2u;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tn.esprit.spring.b2u.entity.*;
import tn.esprit.spring.b2u.repository.*;
import tn.esprit.spring.b2u.entity.WorkMode;
import tn.esprit.spring.b2u.entity.WorkPostStatus;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepo;
    private final EntrepriseRepo entrepriseRepo;
    private final ProjetRepository projetRepo;
    private final WorkPostRepo workPostRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(ApplicationArguments args) {
        // Clear existing seed data and re-seed fresh
        projetRepo.deleteAll();
        workPostRepo.deleteAll();
        userRepo.findByEmail("technova@b2u.tn").ifPresent(u -> { entrepriseRepo.deleteById(u.getEntrepriseId() != null ? u.getEntrepriseId() : ""); userRepo.delete(u); });
        userRepo.findByEmail("dataflow@b2u.tn").ifPresent(u -> { entrepriseRepo.deleteById(u.getEntrepriseId() != null ? u.getEntrepriseId() : ""); userRepo.delete(u); });
        userRepo.findByEmail("student@b2u.tn").ifPresent(u -> userRepo.delete(u));

        // ── Company 1 ──
        User company1User = new User();
        company1User.setFirstName("TechNova");
        company1User.setLastName("Company");
        company1User.setEmail("technova@b2u.tn");
        company1User.setPassword(encoder.encode("Company1!"));
        company1User.setRole("ROLE_COMPANY");
        company1User = userRepo.save(company1User);

        Entreprise e1 = new Entreprise();
        e1.setName("TechNova Solutions");
        e1.setSector("Technologie");
        e1.setEmail("technova@b2u.tn");
        e1.setPhone("71 000 001");
        e1.setAddress("Lac 2, Tunis");
        e1.setUserId(company1User.getId());
        e1 = entrepriseRepo.save(e1);
        company1User.setEntrepriseId(e1.getId());
        userRepo.save(company1User);

        // ── Company 2 ──
        User company2User = new User();
        company2User.setFirstName("DataFlow");
        company2User.setLastName("Company");
        company2User.setEmail("dataflow@b2u.tn");
        company2User.setPassword(encoder.encode("Company1!"));
        company2User.setRole("ROLE_COMPANY");
        company2User = userRepo.save(company2User);

        Entreprise e2 = new Entreprise();
        e2.setName("DataFlow Analytics");
        e2.setSector("Data & IA");
        e2.setEmail("dataflow@b2u.tn");
        e2.setPhone("71 000 002");
        e2.setAddress("El Menzah, Tunis");
        e2.setUserId(company2User.getId());
        e2 = entrepriseRepo.save(e2);
        company2User.setEntrepriseId(e2.getId());
        userRepo.save(company2User);

        // ── Admin ──
        if (userRepo.findByEmail("admin@b2u.tn").isEmpty()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("B2U");
            admin.setEmail("admin@b2u.tn");
            admin.setPassword(encoder.encode("Admin123!"));
            admin.setRole("ROLE_ADMIN");
            userRepo.save(admin);
        }

        // ── Student ──
        if (userRepo.findByEmail("student@b2u.tn").isEmpty()) {
            User student = new User();
            student.setFirstName("Ahmed");
            student.setLastName("Ben Ali");
            student.setEmail("student@b2u.tn");
            student.setPassword(encoder.encode("Student1!"));
            student.setRole("ROLE_STUDENT");
            userRepo.save(student);
        }

        // ── Projets TechNova ──
        Projet p1 = new Projet();
        p1.setTitle("Plateforme e-commerce B2B");
        p1.setDescription("Développement d'une plateforme e-commerce complète avec gestion des commandes, facturation et tableau de bord analytics.");
        p1.setType("STAGE");
        p1.setCompanyId(e1.getId());
        p1.setCompanyName(e1.getName());
        p1.setRequiredSkills(List.of("Angular", "Spring Boot", "MongoDB"));
        p1.setTechnologies(List.of("Angular", "Spring Boot", "MongoDB", "Docker"));
        p1.setTeamSize(3);
        p1.setStatus("open");
        p1.setApplicantsCount(0);
        p1.setCreatedAt(new Date());
        p1.setDeadline(new Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000));
        p1 = projetRepo.save(p1);

        Projet p2 = new Projet();
        p2.setTitle("Application mobile RH");
        p2.setDescription("Création d'une app mobile cross-platform pour la gestion RH : congés, présences et performance des employés.");
        p2.setType("PROJET");
        p2.setCompanyId(e1.getId());
        p2.setCompanyName(e1.getName());
        p2.setRequiredSkills(List.of("Flutter", "Firebase", "Dart"));
        p2.setTechnologies(List.of("Flutter", "Firebase", "Dart"));
        p2.setTeamSize(2);
        p2.setStatus("open");
        p2.setApplicantsCount(0);
        p2.setCreatedAt(new Date());
        p2.setDeadline(new Date(System.currentTimeMillis() + 45L * 24 * 3600 * 1000));
        p2 = projetRepo.save(p2);

        // ── Projets DataFlow ──
        Projet p3 = new Projet();
        p3.setTitle("Dashboard IA prédictif");
        p3.setDescription("Développement d'un dashboard de prédiction des tendances marché basé sur le machine learning et la visualisation de données.");
        p3.setType("STAGE");
        p3.setCompanyId(e2.getId());
        p3.setCompanyName(e2.getName());
        p3.setRequiredSkills(List.of("Python", "TensorFlow", "React"));
        p3.setTechnologies(List.of("Python", "TensorFlow", "React", "PostgreSQL"));
        p3.setTeamSize(2);
        p3.setStatus("open");
        p3.setApplicantsCount(0);
        p3.setCreatedAt(new Date());
        p3.setDeadline(new Date(System.currentTimeMillis() + 60L * 24 * 3600 * 1000));
        p3 = projetRepo.save(p3);

        Projet p4 = new Projet();
        p4.setTitle("ETL Pipeline Big Data");
        p4.setDescription("Mise en place d'un pipeline ETL pour traiter et analyser des millions de transactions en temps réel.");
        p4.setType("PROJET");
        p4.setCompanyId(e2.getId());
        p4.setCompanyName(e2.getName());
        p4.setRequiredSkills(List.of("Spark", "Kafka", "Scala"));
        p4.setTechnologies(List.of("Apache Spark", "Kafka", "Scala", "AWS"));
        p4.setTeamSize(3);
        p4.setStatus("open");
        p4.setApplicantsCount(0);
        p4.setCreatedAt(new Date());
        p4.setDeadline(new Date(System.currentTimeMillis() + 90L * 24 * 3600 * 1000));
        p4 = projetRepo.save(p4);

        // ── WorkPosts TechNova liés aux projets ──
        WorkPost w1 = new WorkPost();
        w1.setTitle("Développeur Full Stack Angular/Spring");
        w1.setRequiredSkills("Angular, Spring Boot, MongoDB, REST API");
        w1.setHoursPerWeek(35);
        w1.setWorkMode(WorkMode.HYBRID);
        w1.setStatus(WorkPostStatus.ACTIVE);
        w1.setEntrepriseId(e1.getId());
        w1.setProjetId(p1.getId());
        workPostRepo.save(w1);

        WorkPost w2 = new WorkPost();
        w2.setTitle("Développeur Mobile Flutter");
        w2.setRequiredSkills("Flutter, Dart, Firebase, UI/UX");
        w2.setHoursPerWeek(30);
        w2.setWorkMode(WorkMode.REMOTE);
        w2.setStatus(WorkPostStatus.ACTIVE);
        w2.setEntrepriseId(e1.getId());
        w2.setProjetId(p2.getId());
        workPostRepo.save(w2);

        WorkPost w3 = new WorkPost();
        w3.setTitle("Data Scientist / ML Engineer");
        w3.setRequiredSkills("Python, TensorFlow, Scikit-learn, SQL");
        w3.setHoursPerWeek(40);
        w3.setWorkMode(WorkMode.ONSITE);
        w3.setStatus(WorkPostStatus.ACTIVE);
        w3.setEntrepriseId(e2.getId());
        w3.setProjetId(p3.getId());
        workPostRepo.save(w3);

        System.out.println("✅ Seed data loaded: 2 companies, 4 projects, 3 work posts, 1 student, 1 admin");
        System.out.println("   Admin:     admin@b2u.tn    / Admin123!");
        System.out.println("   Company 1: technova@b2u.tn / Company1!");
        System.out.println("   Company 2: dataflow@b2u.tn / Company1!");
        System.out.println("   Student:   student@b2u.tn  / Student1!");
    }
}
