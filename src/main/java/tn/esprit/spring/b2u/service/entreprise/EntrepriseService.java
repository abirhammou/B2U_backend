package tn.esprit.spring.b2u.service.entreprise;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.spring.b2u.DTO.EntrepriseDTO;
import tn.esprit.spring.b2u.entity.Entreprise;
import tn.esprit.spring.b2u.exception.DuplicateResourceException;
import tn.esprit.spring.b2u.exception.ResourceNotFoundException;
import tn.esprit.spring.b2u.repository.EntrepriseRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntrepriseService implements IEntrepriseService{

    private final EntrepriseRepo enterpriseRepository;

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
            throw new DuplicateResourceException("An enterprise with name '" + dto.getName() + "' already exists.");
        }

        if (enterpriseRepository.existsByPhone(dto.getPhone())) {
            throw new DuplicateResourceException("An enterprise with phone '" + dto.getPhone() + "' already exists.");
        }

        Entreprise enterprise = new Entreprise();
        enterprise.setName(dto.getName());
        enterprise.setDescription(dto.getDescription());
        enterprise.setSector(dto.getSector());
        enterprise.setAddress(dto.getAddress());
        enterprise.setEmail(dto.getEmail());
        enterprise.setPhone(dto.getPhone());

        enterpriseRepository.save(enterprise);
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
}
