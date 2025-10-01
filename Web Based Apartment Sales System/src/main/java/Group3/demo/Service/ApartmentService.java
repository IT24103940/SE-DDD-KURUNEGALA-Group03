package Group3.demo.Service;

import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.ApartmentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApartmentService {
    @Autowired
    private group3.demo.ApartmentRepository repository;

    private static final String UPLOAD_DIR = "uploads/";  // Create this folder in project root

    public void saveApartment(Apartment apartment, List<MultipartFile> images) {
        Apartment saved = repository.save(apartment);
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty() && isValidImage(image)) {
                    String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                    try {
                        Path uploadPath = Paths.get(UPLOAD_DIR);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
                        group3.demo.ApartmentImage img = new group3.demo.ApartmentImage(saved.getId(), UPLOAD_DIR + fileName);
                        // Save image entity (add repo if needed, or use apartment.getImages().add)
                        saved.getImages().add(img);  // Assuming bidirectional
                    } catch (IOException e) {
                        // Log error
                    }
                }
            }
        }
        repository.save(saved);
    }

    public Apartment updateApartment(Integer id, Apartment updatedApartment, List<MultipartFile> newImages) {
        Apartment existing = repository.findById(id).orElseThrow();
        existing.setTitle(updatedApartment.getTitle());
        existing.setLocation(updatedApartment.getLocation());
        existing.setSizeSqft(updatedApartment.getSizeSqft());
        existing.setPrice(updatedApartment.getPrice());
        existing.setDescription(updatedApartment.getDescription());
        existing.setStatus(updatedApartment.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        // Handle new images similarly to save
        if (newImages != null && !newImages.isEmpty()) {
            // Delete old images logic here if needed (e.g., remove files and DB entries)
            for (MultipartFile image : newImages) {
                if (!image.isEmpty() && isValidImage(image)) {
                    String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                    try {
                        Path uploadPath = Paths.get(UPLOAD_DIR);
                        Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
                        group3.demo.ApartmentImage img = new group3.demo.ApartmentImage(id, UPLOAD_DIR + fileName);
                        existing.getImages().add(img);
                    } catch (IOException e) {
                        // Log error
                    }
                }
            }
        }
        return repository.save(existing);
    }

    public void archiveApartment(Integer id) {
        Apartment apt = repository.findById(id).orElseThrow();
        apt.setStatus(ApartmentStatus.ARCHIVED);
        repository.save(apt);
    }

    public List<Apartment> searchApartments(String location, ApartmentStatus status, Double minPrice, Double maxPrice) {
        return repository.searchApartments(location, status, minPrice, maxPrice);
    }

    private boolean isValidImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (contentType.equals("image/jpeg") || contentType.equals("image/png"))
                && file.getSize() <= 5 * 1024 * 1024;  // 5MB limit
    }

    // Other methods: getById, getAll, etc.
    public List<Apartment> getAll() { return repository.findAll(); }
    public Apartment getById(Integer id) { return repository.findById(id).orElse(null); }
}