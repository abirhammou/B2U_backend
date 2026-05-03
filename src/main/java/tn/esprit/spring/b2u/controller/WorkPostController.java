package tn.esprit.spring.b2u.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.spring.b2u.entity.WorkPost;
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


    // ✔ créer mission
    @PostMapping("/add")
    public WorkPost create(@RequestBody WorkPost post, Principal principal) {

        String entrepriseId = principal.getName(); // ou depuis JWT
        post.setEntrepriseId(entrepriseId);
        return workPostService.create(post);
    }


    // ✔ toutes les missions
    @GetMapping("/all")
    public List<WorkPost> getAll() {
        return workPostService.getAll();
    }


    // ✔ missions par entreprise
    @GetMapping("/entreprise/{id}")
    public List<WorkPost> getByEntreprise(@PathVariable String id) {
        return workPostService.getByEntreprise(id);
    }

    // ✔ update
    @PutMapping("/update/{id}")
    public WorkPost update(@PathVariable String id, @RequestBody WorkPost post) {
        return workPostService.update(id, post);
    }


    // ✔ delete
    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable String id) {
        workPostService.delete(id);
    }


    @GetMapping("/recommended")
    public List<WorkPost> getRecommended(@RequestParam int maxHours) {
        return workPostService.getRecommendedPosts(maxHours);
    }

    @GetMapping("/generate")
    public WorkPost generateWithAI(@RequestParam String title, @RequestParam String sector) {
        return aiWorkPostService.generateWorkPost(title, sector);
    }
}
