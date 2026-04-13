package tn.esprit.spring.b2u.service;

import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.security.JwtUtil;
import tn.esprit.spring.b2u.DTO.RegisterRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // ================= REGISTER =================
    public String register(RegisterRequest request) {

        // 🔴 Validation prénom
        if (request.getFirstName() == null || request.getFirstName().length() < 2) {
            throw new RuntimeException("Le prénom est invalide");
        }

        // 🔴 Validation nom
        if (request.getLastName() == null || request.getLastName().length() < 2) {
            throw new RuntimeException("Le nom est invalide");
        }

        // 🔴 Validation email
        if (request.getEmail() == null ||
                !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Email invalide");
        }

        // 🔴 Validation password
        if (request.getPassword() == null ||
                !request.getPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {

            throw new RuntimeException(
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre"
            );
        }

        // 🔴 Email déjà utilisé
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // 🔁 Création User
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // 🔐 rôle
        switch (request.getRole() != null ? request.getRole().toLowerCase() : "") {
            case "admin"   -> user.setRole("ROLE_ADMIN");
            case "company" -> user.setRole("ROLE_COMPANY");
            default        -> user.setRole("ROLE_STUDENT");
        }


        userRepo.save(user);

        return "Utilisateur créé avec succès";
    }

    // ================= LOGIN =================
    public Map<String, Object> login(String email, String password) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new RuntimeException("Invalid password");

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(email, user.getRole()));
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());
        return response;
    }

}