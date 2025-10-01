package Group3.demo.Controller;


import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.ApartmentStatus;
import Group3.demo.Service.ApartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
@RequestMapping("/admin/apartments")
public class AdminApartmentController {
    @Autowired
    private ApartmentService service;

    @GetMapping
    public String listApartments(Model model,
                                 @RequestParam(required = false) String location,
                                 @RequestParam(required = false) ApartmentStatus status,
                                 @RequestParam(required = false) Double minPrice,
                                 @RequestParam(required = false) Double maxPrice) {
        List<Apartment> apartments = service.searchApartments(location, status, minPrice, maxPrice);
        model.addAttribute("apartments", apartments);
        model.addAttribute("location", location);
        model.addAttribute("status", status);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        return "admin/apartment-list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("apartment", new Apartment());
        return "admin/apartment-add";
    }

    @PostMapping("/add")
    public String addApartment(@ModelAttribute Apartment apartment,
                               @RequestParam("images") List<MultipartFile> images,
                               Model model) {
        try {
            service.saveApartment(apartment, images);
            return "redirect:/admin/apartments";
        } catch (Exception e) {
            model.addAttribute("error", "Validation failed: " + e.getMessage());
            return "admin/apartment-add";
        }
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Apartment apt = service.getById(id);
        model.addAttribute("apartment", apt);
        return "admin/apartment-edit";
    }

    @PostMapping("/edit/{id}")
    public String updateApartment(@PathVariable Integer id,
                                  @ModelAttribute Apartment apartment,
                                  @RequestParam("images") List<MultipartFile> images,
                                  Model model) {
        try {
            service.updateApartment(id, apartment, images);
            return "redirect:/admin/apartments";
        } catch (Exception e) {
            model.addAttribute("error", "Update failed: " + e.getMessage());
            return "admin/apartment-edit";
        }
    }

    @PostMapping("/archive/{id}")
    public String archiveApartment(@PathVariable Integer id) {
        service.archiveApartment(id);
        return "redirect:/admin/apartments";
    }
}
