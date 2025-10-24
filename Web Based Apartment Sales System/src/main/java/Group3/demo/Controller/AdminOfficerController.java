package Group3.demo.Controller;

import Group3.demo.DTO.ApartmentDTO;
import Group3.demo.Entity.Apartment;
import Group3.demo.Repository.ApartmentRepository;
import Group3.demo.Service.ApartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOfficerController {

    private final ApartmentService apartmentService;
    private final ApartmentRepository apartmentRepo;

    // List apartments
    @GetMapping("/apartments")
    public String list(Model model) {
        model.addAttribute("apartments", apartmentService.list());
        return "admin/apartments";
    }

    // Show create form
    @GetMapping("/apartments/new")
    public String newForm(Model model) {
        model.addAttribute("apartment", new ApartmentDTO());
        return "admin/apartment-form";
    }

    // Handle create
    @PostMapping("/apartments")
    public String create(@ModelAttribute("apartment") @Valid ApartmentDTO dto) {
        apartmentService.create(dto);
        return "redirect:/admin/apartments";
    }

    // Show edit form
    @GetMapping("/apartments/{id}/edit")
    public String edit(@PathVariable("id") Integer id, Model model) {
        Apartment ap = apartmentService.get(id);
        if (ap == null) {
            throw new IllegalArgumentException("Apartment not found with id " + id);
        }

        ApartmentDTO dto = new ApartmentDTO();
        dto.setCode(ap.getCode());
        dto.setTitle(ap.getTitle());
        dto.setDescription(ap.getDescription());
        dto.setCity(ap.getCity());
        dto.setPrice(ap.getPrice());
        dto.setStatus(ap.getStatus());
        dto.setBedrooms(ap.getBedrooms());
        dto.setBathrooms(ap.getBathrooms());
        dto.setAreaSqFt(ap.getAreaSqFt());

        model.addAttribute("apartmentId", id);
        model.addAttribute("apartment", dto);
        return "admin/apartment-form";
    }

    // Handle update
    @PostMapping("/apartments/{id}")
    public String update(@PathVariable("id") Integer id,
                         @ModelAttribute("apartment") @Valid ApartmentDTO dto) {
        apartmentService.update(id, dto);
        return "redirect:/admin/apartments";
    }

    // Handle delete
    @PostMapping("/apartments/{id}/delete")
    public String delete(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            apartmentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Apartment deleted successfully.");
        } catch (IllegalStateException ex) {
            // expected case: leads reference this apartment
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            // fallback
            redirectAttributes.addFlashAttribute("error", "Unable to delete apartment. Please contact admin.");
        }
        return "redirect:/admin/apartments";
    }

    // In AdminOfficerController
    @PostMapping("/apartment/{id}/upload")
    public String uploadApartmentImage(@PathVariable("id") Integer id,
                                       @RequestParam("image") MultipartFile file) throws IOException {
        var apartment = apartmentRepo.findById(id).orElseThrow();
        String fileName = "apartment_" + id + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/apartments");
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        apartment.setImageUrl("/uploads/apartments/" + fileName);
        apartmentRepo.save(apartment);

        return "redirect:/admin/apartments";
    }

}
