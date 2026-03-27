package tn.esprit.spring.b2u.controller;

import jakarta.validation.Valid;
import tn.esprit.spring.b2u.DTO.RegisterRequest;
import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> req) {

        String token = authService.login(
                req.get("username"),
                req.get("password")
        );

        return Map.of("token", token);
    }
}