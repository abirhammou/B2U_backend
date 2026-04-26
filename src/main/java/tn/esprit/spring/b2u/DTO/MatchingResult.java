package tn.esprit.spring.b2u.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchingResult {
    private int score;
    private String recommendation;
    private String level;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private Map<String, Integer> skillScores;
    private String summary;
}