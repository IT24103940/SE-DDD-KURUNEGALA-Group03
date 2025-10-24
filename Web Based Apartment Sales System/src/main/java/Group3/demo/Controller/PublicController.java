package Group3.demo.Controller;

import Group3.demo.Repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller @RequiredArgsConstructor
public class PublicController {
    private final PromotionRepository promotionRepository;

    @GetMapping("/public/promotions")
    public String viewPromotions(Model model) {
        model.addAttribute("promotions", promotionRepository.findAll());
        return "public/promotions";
    }
}
