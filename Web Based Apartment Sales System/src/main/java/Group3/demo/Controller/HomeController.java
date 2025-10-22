package Group3.demo.Controller;

import Group3.demo.Repository.ApartmentRepository;
import Group3.demo.Repository.PromotionRepository;
import Group3.demo.Service.FeedbackService;
import Group3.demo.Service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import Group3.demo.Repository.CustomerRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.math.RoundingMode;

import Group3.demo.Entity.enums.ApartmentStatus;
import Group3.demo.Entity.enums.PromotionStatus;
import Group3.demo.Entity.Promotion;
import Group3.demo.Entity.Apartment;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ApartmentRepository apartmentRepo;
    private final PromotionRepository promoRepo;
    private final CustomerRepository customerRepo;
    private final FeedbackService feedbackService;
    private final DiscountService discountService;

    @GetMapping("/home")
    public String home(
            Model model,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "minBedrooms", required = false) Integer minBedrooms,
            @RequestParam(name = "maxBedrooms", required = false) Integer maxBedrooms,
            @RequestParam(name = "minBathrooms", required = false) Integer minBathrooms,
            @RequestParam(name = "maxBathrooms", required = false) Integer maxBathrooms,
            @RequestParam(name = "minArea", required = false) Integer minArea,
            @RequestParam(name = "maxArea", required = false) Integer maxArea,
            @RequestParam(name = "promo", required = false, defaultValue = "all") String promo,
            @RequestParam(name = "sort", required = false, defaultValue = "newest") String sort,
            @RequestParam(name = "successMessage", required = false) String successMessage
    ) {
        // Normalize blank strings to null for optional filters
        String qNorm = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        String cityNorm = (city != null && !city.trim().isEmpty()) ? city.trim() : null;

        // Normalize price range
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal tmp = minPrice;
            minPrice = maxPrice;
            maxPrice = tmp;
        }
        // Normalize integer ranges
        if (minBedrooms != null && maxBedrooms != null && minBedrooms > maxBedrooms) {
            int t = minBedrooms; minBedrooms = maxBedrooms; maxBedrooms = t;
        }
        if (minBathrooms != null && maxBathrooms != null && minBathrooms > maxBathrooms) {
            int t = minBathrooms; minBathrooms = maxBathrooms; maxBathrooms = t;
        }
        if (minArea != null && maxArea != null && minArea > maxArea) {
            int t = minArea; minArea = maxArea; maxArea = t;
        }

        Boolean hasPromotion = null; // all
        if ("with".equalsIgnoreCase(promo)) hasPromotion = true;
        else if ("without".equalsIgnoreCase(promo)) hasPromotion = false;

        List<Group3.demo.Entity.Apartment> apartments = apartmentRepo.searchAvailable(
                ApartmentStatus.AVAILABLE,
                qNorm,
                cityNorm,
                minPrice,
                maxPrice,
                minBedrooms,
                maxBedrooms,
                minBathrooms,
                maxBathrooms,
                minArea,
                maxArea,
                hasPromotion,
                PromotionStatus.ACTIVE
        );

        // Apply sorting (default newest)
        if ("price_asc".equalsIgnoreCase(sort)) {
            apartments.sort(Comparator.comparing(a -> a.getPrice() == null ? BigDecimal.valueOf(-1) : a.getPrice()));
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            apartments.sort(Comparator.comparing((Group3.demo.Entity.Apartment a) -> a.getPrice() == null ? BigDecimal.valueOf(-1) : a.getPrice()).reversed());
        } else if ("oldest".equalsIgnoreCase(sort)) {
            apartments.sort(Comparator.comparing(Group3.demo.Entity.Apartment::getCreatedAt));
        } else { // newest
            apartments.sort(Comparator.comparing(Group3.demo.Entity.Apartment::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        }

        model.addAttribute("apartments", apartments);
        model.addAttribute("resultsCount", apartments.size());
        model.addAttribute("promotions", promoRepo.findAll());
        model.addAttribute("customers", customerRepo.findAll());
        model.addAttribute("feedbackList", feedbackService.getApprovedFeedback());
        model.addAttribute("feedback", new Group3.demo.Entity.Feedback()); // For feedback form binding
        model.addAttribute("cities", apartmentRepo.findDistinctCities(ApartmentStatus.AVAILABLE));

        // Pass through flash messages
        if (successMessage != null && !successMessage.isBlank()) {
            model.addAttribute("successMessage", successMessage);
        }

        // Echo filters back to the view for prefill
        model.addAttribute("q", q);
        model.addAttribute("city", city);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("minBedrooms", minBedrooms);
        model.addAttribute("maxBedrooms", maxBedrooms);
        model.addAttribute("minBathrooms", minBathrooms);
        model.addAttribute("maxBathrooms", maxBathrooms);
        model.addAttribute("minArea", minArea);
        model.addAttribute("maxArea", maxArea);
        model.addAttribute("promo", promo);
        model.addAttribute("sort", sort);
        return "home"; // ✅ or "home/index" depending on your template
    }

    @GetMapping("/apartment/{id}")
    public String apartmentDetail(@PathVariable("id") Integer id, Model model,
                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        return apartmentRepo.findById(id)
                .map(apartment -> {
                    model.addAttribute("apartment", apartment);
                    if (currentUser != null) {
                        return "apartment-detail";
                    } else {
                        return "redirect:/customer/auth"; // ✅ fixed
                    }
                })
                .orElse("redirect:/");
    }

    @GetMapping("/promotion/{id}")
    public String promotionDetail(@PathVariable("id") Integer id, Model model,
                                  @AuthenticationPrincipal org.springframework.security.core.userdetails.User currentUser) {
        Promotion promoObj = promoRepo.findById(id).orElse(null);
        if (promoObj == null) return "redirect:/";
        final Promotion promoFinal = promoObj;

        model.addAttribute("promotion", promoFinal);

        // Build applicable apartments list and discounted prices map
        List<Apartment> applicable;
        if (promoFinal.getApartment() != null) {
            var apt = promoFinal.getApartment();
            if (apt != null && apt.getStatus() == ApartmentStatus.AVAILABLE && isPromotionApplicable(promoFinal)) {
                applicable = java.util.List.of(apt);
            } else {
                applicable = java.util.List.of();
            }
        } else {
            // Applies to all available apartments
            applicable = apartmentRepo.findByStatus(ApartmentStatus.AVAILABLE)
                    .stream()
                    .filter(a -> isPromotionApplicable(promoFinal))
                    .toList();
        }
        Map<Integer, BigDecimal> discounted = new HashMap<>();
        for (Apartment a : applicable) {
            BigDecimal orig = a.getPrice() != null ? a.getPrice() : BigDecimal.ZERO;
            BigDecimal disc = discountService.calculateDiscount(promoFinal, orig);
            if (disc.compareTo(orig) > 0) disc = orig;
            discounted.put(a.getId(), orig.subtract(disc).setScale(2, RoundingMode.HALF_UP));
        }
        model.addAttribute("apartments", applicable);
        model.addAttribute("discountedPrices", discounted);

        if (currentUser != null) {
            return "promotion-detail";
        } else {
            return "redirect:/customer/auth"; // ✅ fixed
        }
    }

    // Simple applicability by date/status. If the promotion targets a specific apartment,
    // we already filter by it above; otherwise it is considered global.
    private boolean isPromotionApplicable(Promotion p) {
        if (p.getStatus() != PromotionStatus.ACTIVE) return false;
        LocalDate today = LocalDate.now();
        if (p.getStartDate() != null && today.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && today.isAfter(p.getEndDate())) return false;
        return true;
    }

    // discount calculation moved to DiscountService
}
