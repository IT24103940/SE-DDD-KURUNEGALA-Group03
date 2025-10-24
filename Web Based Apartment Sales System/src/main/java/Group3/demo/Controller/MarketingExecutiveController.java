package Group3.demo.Controller;

import Group3.demo.Entity.Promotion;
import Group3.demo.Entity.User;
import Group3.demo.Entity.Apartment;
import Group3.demo.Entity.enums.PromotionStatus;
import Group3.demo.Repository.ApartmentRepository;
import Group3.demo.Repository.PromotionRepository;
import Group3.demo.Repository.UserRepository;
import Group3.demo.Repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Controller
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class MarketingExecutiveController {

    private final PromotionRepository promotionRepo;
    private final ApartmentRepository apartmentRepo;
    private final UserRepository userRepo;
    private final SalesOrderRepository salesOrderRepo;

    // Dashboard landing
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "marketing/dashboard";
    }

    // Add inside your MarketingExecutiveController
    @GetMapping("/performance")
    public String showPerformanceVisualizer() {
        return "marketing/performance";
    }

    @GetMapping("/scheduler")
    public String showSocialScheduler() {
        return "marketing/scheduler";
    }

    @GetMapping("/marketing")
    public String showMarketingDashboard() {
        return "marketing/dashboard";
    }


    /* ---------------- Promotions ---------------- */

    // List all promotions + create form
    @GetMapping("/promotions")
    public String listPromotions(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("promotions", promotionRepo.findAll());
        model.addAttribute("statuses", PromotionStatus.values());
        model.addAttribute("apartments", apartmentRepo.findAll());
        if (error != null) model.addAttribute("errorMessage", error);
        return "marketing/promotions";  // keep file inside templates/
    }

    // Create promotion
    @PostMapping("/promotions")
    public String createPromotion(@ModelAttribute Promotion promotion, Authentication auth) {
        // Get logged-in user
        User currentUser = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + auth.getName()));

        // Rebind nested apartment id to a managed entity to avoid transient reference issues
        if (promotion.getApartment() != null && promotion.getApartment().getId() != null) {
            Integer aptId = promotion.getApartment().getId();
            Optional<Apartment> apt = apartmentRepo.findById(aptId);
            promotion.setApartment(apt.orElse(null));
        } else {
            promotion.setApartment(null);
        }

        // If discount type is not selected, clear discount value to avoid confusion
        if (promotion.getDiscountType() == null) {
            promotion.setDiscountValue(null);
        }

        promotion.setCreatedBy(currentUser); // ✅ set creator
        promotionRepo.save(promotion);
        return "redirect:/marketing/promotions";
    }

    // Delete promotion (guard if referenced by any sales order)
    @PostMapping("/promotions/{id}/delete")
    public String deletePromotion(@PathVariable("id") Integer id) {
        long inUse = salesOrderRepo.countByPromotionId(id);
        if (inUse > 0) {
            String msg = "Cannot delete promotion: it is linked to " + inUse + " sales order(s). Set status to INACTIVE instead.";
            return "redirect:/marketing/promotions?error=" + java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8);
        }
        try {
            promotionRepo.deleteById(id);
            return "redirect:/marketing/promotions";
        } catch (Exception ex) {
            String msg = "Cannot delete promotion due to existing references.";
            return "redirect:/marketing/promotions?error=" + java.net.URLEncoder.encode(msg, java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    // Edit form
    @GetMapping("/promotions/{id}/edit")
    public String editPromotion(@PathVariable("id") Integer id, Model model) {
        Promotion promotion = promotionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid promotion Id:" + id));

        model.addAttribute("promotion", promotion);
        model.addAttribute("statuses", PromotionStatus.values());
        model.addAttribute("apartments", apartmentRepo.findAll());
        return "marketing/edit-promotion";
    }

    // Update promotion
    @PostMapping("/promotions/{id}/update")
    public String updatePromotion(@PathVariable("id") Integer id,
                                  @ModelAttribute Promotion promotion,
                                  Authentication auth) {
        // Get logged-in user again
        User currentUser = userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + auth.getName()));

        // Rebind nested apartment id to a managed entity
        if (promotion.getApartment() != null && promotion.getApartment().getId() != null) {
            Integer aptId = promotion.getApartment().getId();
            Optional<Apartment> apt = apartmentRepo.findById(aptId);
            promotion.setApartment(apt.orElse(null));
        } else {
            promotion.setApartment(null);
        }

        if (promotion.getDiscountType() == null) {
            promotion.setDiscountValue(null);
        }

        promotion.setId(id);
        promotion.setCreatedBy(currentUser); // ✅ required to avoid null
        promotionRepo.save(promotion);
        return "redirect:/marketing/promotions";
    }

    // In MarketingExecutiveController
    @PostMapping("/promotion/{id}/upload")
    public String uploadPromoImage(@PathVariable("id") Integer id,
                                   @RequestParam("image") MultipartFile file) throws IOException {
        var promo = promotionRepo.findById(id).orElseThrow();
        String fileName = "promo_" + id + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get("uploads/promotions");
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        promo.setBannerImageUrl("/uploads/promotions/" + fileName);
        promotionRepo.save(promo);

        return "redirect:/marketing/promotions";
    }

}
