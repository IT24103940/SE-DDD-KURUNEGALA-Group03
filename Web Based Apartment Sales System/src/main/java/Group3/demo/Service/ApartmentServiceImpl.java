package Group3.demo.Service.Impl;

import Group3.demo.DTO.ApartmentDTO;
import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.enums.ApartmentStatus;
import Group3.demo.Repository.ApartmentRepository;
import Group3.demo.Service.ApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {

    private final ApartmentRepository repo;

    @Override
    public Apartment create(ApartmentDTO dto) {
        return repo.save(toEntity(new Apartment(), dto));
    }

    @Override
    public Apartment update(Integer id, ApartmentDTO dto) {
        return repo.findById(id)
                .map(ap -> repo.save(toEntity(ap, dto)))
                .orElseThrow(() -> new IllegalArgumentException("Apartment not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new IllegalArgumentException("Apartment not found with id " + id);
        }
    }

    @Override
    public List<Apartment> list() {
        return repo.findAll();
    }

    @Override
    public Apartment get(Integer id) {
        return repo.findById(id).orElse(null); // return null if not found
    }

    private Apartment toEntity(Apartment ap, ApartmentDTO dto) {
        ap.setCode(dto.getCode());
        ap.setTitle(dto.getTitle());
        ap.setDescription(dto.getDescription());
        ap.setCity(dto.getCity());
        ap.setPrice(dto.getPrice());
        ap.setStatus(dto.getStatus() != null ? dto.getStatus() : ApartmentStatus.AVAILABLE);
        ap.setBedrooms(dto.getBedrooms());
        ap.setBathrooms(dto.getBathrooms());
        ap.setAreaSqFt(dto.getAreaSqFt());
        return ap;
    }
}
