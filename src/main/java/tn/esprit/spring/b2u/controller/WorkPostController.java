package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.WorkPost;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.entity.User;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.EntrepriseRepo;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.service.workPost.AiWorkPostService;
import tn.esprit.spring.b2u.service.workPost.IWorkPostService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/workpost")
@RequiredArgsConstructor
public class WorkPostController {

    private final IWorkPostService workPostService;
    private final AiWorkPostService aiWorkPostService;
    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final EntrepriseRepo entrepriseRepo;

    private String resolveEntrepriseId(User user) {
        if (user.getEntrepriseId() != null) return user.getEntrepriseId();
        Entreprise enterprise = new Entreprise();
        enterprise.setName(user.getFirstName() + " " + user.getLastName());
        enterprise.setEmail(user.getEmail());
        enterprise.setUserId(user.getId());
        enterprise = entrepriseRepo.save(enterprise);
        user.setEntrepriseId(enterprise.getId());
        userRepository.save(user);
        return enterprise.getId();
    }

    @PostMapping("/add")
    public WorkPost create(@RequestBody WorkPost post, Principal principal) {

        String entrepriseId = principal.getName(); // ou depuis JWT
        post.setEntrepriseId(entrepriseId);
        return workPostService.create(post);
    }

    @GetMapping("/all")
    public List<WorkPost> getAll() {
        return workPostService.getAll();
    }

    @GetMapping("/entreprise/{id}")
    public List<WorkPost> getByEntreprise(@PathVariable String id) {
        return workPostService.getByEntreprise(id);
    }

    @PutMapping("/update/{id}")
    public WorkPost update(@PathVariable String id, @RequestBody WorkPost post) {
        return workPostService.update(id, post);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        workPostService.delete(id);
    }

    @GetMapping("/{id}")
    public WorkPost getById(@PathVariable String id) {
        return workPostService.getById(id);
    }

    @GetMapping("/recommended")
    public List<WorkPost> getRecommended(@RequestParam int maxHours) {
        return workPostService.getRecommendedPosts(maxHours);
    }

    @GetMapping("/generate")
    public WorkPost generateWithAI(@RequestParam String title, @RequestParam String sector) {
        return aiWorkPostService.generateWorkPost(title, sector);
    }

    @PutMapping("/{workPostId}/assign-projet/{projetId}")
    public WorkPost assignProjet(
            @PathVariable String workPostId,
            @PathVariable String projetId) {
        return workPostService.assignProjet(workPostId, projetId);
    }

    @PutMapping("/{workPostId}/unassign-projet")
    public WorkPost unassignProjet(@PathVariable String workPostId) {
        return workPostService.unassignProjet(workPostId);
    }

    @GetMapping("/projet/{projetId}")
    public List<WorkPost> getByProjet(@PathVariable String projetId) {
        return workPostService.getByProjet(projetId);
    }

    @GetMapping("/projet/{id}")
    public Projet getProjet(@PathVariable String id) {
        return projetRepository.findById(id).orElse(null);
    }
}