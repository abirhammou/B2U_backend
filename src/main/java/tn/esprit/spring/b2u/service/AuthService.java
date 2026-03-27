package tn.esprit.spring.b2u.service;

import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.security.JwtUtil;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.RegisterRequest;
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

    public String register(RegisterRequest request) {

        // 🔴 validation username
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            throw new RuntimeException("Le username doit contenir au moins 3 caractères");
        }

        // 🔴 validation email
        if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RuntimeException("Email invalide");
        }

        // 🔴 validation password
        if (request.getPassword() == null ||
                !request.getPassword().matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {

            throw new RuntimeException(
                    "Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre"
            );
        }

        // 🔴 vérifier email existe déjà
        if (userRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // 🔴 vérifier username existe déjà
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Ce username est déjà utilisé");
        }

        // 🔁 conversion DTO → Entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // rôle
        if ("ADMIN".equals(request.getRole())) {
            user.setRole("ROLE_ADMIN");
        } else {
            user.setRole("ROLE_USER");
        }

        userRepo.save(user);

        return "Utilisateur créé avec succès";
    }
    public String login(String username, String password) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateToken(username);
    }
}