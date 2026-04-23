package tn.esprit.spring.b2u.controller;

import jakarta.validation.Valid;
import tn.esprit.spring.b2u.DTO.RegisterRequest;
import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
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
    public Map<String, Object> login(@RequestBody Map<String, String> req) {
        Map<String, Object> result = authService.login(req.get("email"), req.get("password"));
        return result;
    }

}