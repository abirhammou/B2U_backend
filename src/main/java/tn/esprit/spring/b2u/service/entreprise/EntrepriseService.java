package tn.esprit.spring.b2u.service.entreprise;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.EntrepriseDTO;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.exception.DuplicateResourceException;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.EntrepriseRepo;
import tn.esprit.spring.b2u.repository.UserRepository;
import tn.esprit.spring.b2u.entity.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EntrepriseService implements IEntrepriseService{

    private final EntrepriseRepo enterpriseRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Entreprise> getAllEnterprises() {
        return enterpriseRepository.findAll();
    }

    @Override
    public Optional<Entreprise> getEnterpriseById(String id) {
        return enterpriseRepository.findById(id);
    }

    @Override
    public void createEnterprise(EntrepriseDTO dto) {

        if (enterpriseRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Enterprise already exists");
        }

        // 1. Créer user company
        User user = new User();
        user.setFirstName(dto.getName());
        user.setLastName("Company");
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode("company123")); // password par défaut
        user.setRole("ROLE_COMPANY");

        user = userRepository.save(user);

        // 2. Créer entreprise
        Entreprise enterprise = new Entreprise();
        enterprise.setName(dto.getName());
        enterprise.setDescription(dto.getDescription());
        enterprise.setSector(dto.getSector());
        enterprise.setAddress(dto.getAddress());
        enterprise.setEmail(dto.getEmail());
        enterprise.setPhone(dto.getPhone());

        enterprise.setUserId(user.getId()); // 🔗 lien

        enterpriseRepository.save(enterprise);

        // 3. update user avec entrepriseId
        user.setEntrepriseId(enterprise.getId());
        userRepository.save(user);
    }

    @Override
    public Entreprise updateEnterprise(String id, EntrepriseDTO dto) {
        Entreprise existing = enterpriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enterprise not found with id: " + id));

        if (!existing.getName().equals(dto.getName()) &&
                enterpriseRepository.existsByNameAndIdNot(dto.getName(), id)) {
            throw new DuplicateResourceException("An enterprise with name '" + dto.getName() + "' already exists.");
        }

        if (!existing.getPhone().equals(dto.getPhone()) &&
                enterpriseRepository.existsByPhoneAndIdNot(dto.getPhone(), id)) {
            throw new DuplicateResourceException("An enterprise with phone '" + dto.getPhone() + "' already exists.");
        }

        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        existing.setSector(dto.getSector());
        existing.setAddress(dto.getAddress());
        existing.setEmail(dto.getEmail());
        existing.setPhone(dto.getPhone());

        return enterpriseRepository.save(existing);
    }

    @Override
    public void deleteEnterprise(String id) {
        enterpriseRepository.deleteById(id);
    }

    @Override
    public long getTotalCompaniesCount() {
        return enterpriseRepository.count();
    }

    @Override
    public Map<String, Long> getCountBySector() {
        return enterpriseRepository.findAll()
                .stream()
                .filter(entreprise -> entreprise.getSector() != null) // ignore null sectors
                .collect(Collectors.groupingBy(
                        Entreprise::getSector,
                        Collectors.counting()
                ));
    }

    @Override
    public List<Entreprise> getSimilar(String id) {

        Entreprise e = enterpriseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entreprise not found with id: " + id));

        String sector = e.getSector();

        if (sector == null) {
            return List.of(); // rien à comparer
        }

        return enterpriseRepository.findAll()
                .stream()
                .filter(ent -> ent.getSector() != null)
                .filter(ent -> sector.equals(ent.getSector()))
                .filter(ent -> !ent.getId().equals(id))
                .collect(Collectors.toList());
    }
}
