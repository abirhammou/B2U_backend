package tn.esprit.spring.b2u.service.sprint;

import tn.esprit.spring.b2u.entity.Sprint;
import java.util.List;
import java.util.Optional;

public interface ISprintService {
    List<Sprint> getAllSprints();
    Optional<Sprint> getSprintById(String id);
    List<Sprint> getSprintsByProjet(String projetId);
    Sprint createSprint(Sprint sprint);
    Sprint updateSprint(String id, Sprint sprint);
    void deleteSprint(String id);
    List<Sprint> generateSprintsForProjet(String projetId);
}