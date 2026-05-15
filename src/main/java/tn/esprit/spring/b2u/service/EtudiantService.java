package tn.esprit.spring.b2u.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.entity.Etudiant;
import tn.esprit.spring.b2u.repository.EtudiantRepo;

@Service
@RequiredArgsConstructor
public class EtudiantService {

    private final EtudiantRepo etudiantRepo;

    public Etudiant getByUserId(String userId) {
        return etudiantRepo.findByUserId(userId).orElse(null);
    }

    public Etudiant upsert(String userId, Etudiant data) {
        Etudiant etudiant = etudiantRepo.findByUserId(userId).orElse(new Etudiant());
        etudiant.setUserId(userId);
        etudiant.setBio(data.getBio());
        etudiant.setPhone(data.getPhone());
        etudiant.setUniversity(data.getUniversity());
        etudiant.setGraduationYear(data.getGraduationYear());
        etudiant.setSpecialite(data.getSpecialite());
        etudiant.setLinkedin(data.getLinkedin());
        etudiant.setGithub(data.getGithub());
        etudiant.setSkills(data.getSkills());
        if (data.getAvatarUrl() != null) etudiant.setAvatarUrl(data.getAvatarUrl());
        return etudiantRepo.save(etudiant);
    }
}
