package tn.esprit.spring.b2u.service.sprint;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Projet;
import tn.esprit.spring.b2u.entity.Sprint;
import tn.esprit.spring.b2u.repository.ProjetRepository;
import tn.esprit.spring.b2u.repository.SprintRepo;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SprintService implements ISprintService {

    private final SprintRepo sprintRepo;
    private final ProjetRepository projetRepository;

    @Override
    public List<Sprint> getAllSprints() {
        return sprintRepo.findAll();
    }

    @Override
    public Optional<Sprint> getSprintById(String id) {
        return sprintRepo.findById(id);
    }

    @Override
    public List<Sprint> getSprintsByProjet(String projetId) {
        return sprintRepo.findByProjetId(projetId);
    }

    @Override
    public Sprint createSprint(Sprint sprint) {
        return sprintRepo.save(sprint);
    }

    @Override
    public Sprint updateSprint(String id, Sprint sprint) {
        sprint.setId(id);
        return sprintRepo.save(sprint);
    }

    @Override
    public void deleteSprint(String id) {
        sprintRepo.deleteById(id);
    }

    @Override
    public List<Sprint> generateSprintsForProjet(String projetId) {
        Optional<Projet> projetOpt = projetRepository.findById(projetId);
        if (projetOpt.isEmpty()) return List.of();

        Projet projet = projetOpt.get();
        List<Sprint> sprints = new ArrayList<>();

        // Générer 3 sprints automatiquement
        Date startDate = new Date();
        for (int i = 1; i <= 3; i++) {
            Sprint sprint = new Sprint();
            sprint.setProjetId(projetId);
            sprint.setName("Sprint " + i);
            sprint.setNumber(i);
            sprint.setStatus(i == 1 ? "active" : "planned");
            sprint.setStartDate(startDate);

            // Chaque sprint dure 2 semaines
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DAY_OF_MONTH, 14);
            Date endDate = cal.getTime();
            sprint.setEndDate(endDate);
            startDate = endDate;

            sprints.add(sprintRepo.save(sprint));
        }
        return sprints;
    }
}