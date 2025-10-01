package Group3.demo.Controller;

import com.group3.demo.dto.AdvertisementDTO;
import com.group3.demo.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/marketing")
public class MarketingController {
    @Autowired
    private AdvertisementService service;

    // Marketing Exec: List all campaigns
    @GetMapping("/campaigns")
    public String listCampaigns(Model model) {
        List<AdvertisementDTO> campaigns = service.getAll();
        model.addAttribute("campaigns", campaigns);
        return "marketing/list-campaigns";
    }

    // Marketing Exec: Create form
    @GetMapping("/campaigns/create")
    public String createForm(Model model) {
        model.addAttribute("advertisement", new AdvertisementDTO());
        return "marketing/create-campaign";
    }

    // Marketing Exec: Create post
    @PostMapping("/campaigns")
    public String create(@ModelAttribute AdvertisementDTO dto) {
        service.create(dto, "marketing_user");  // Replace with actual user from security context
        return "redirect:/marketing/campaigns";
    }

    // Marketing Exec: Update form
    @GetMapping("/campaigns/update/{id}")
    public String updateForm(@PathVariable Long id, Model model) {
        AdvertisementDTO dto = service.getAll().stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
        model.addAttribute("advertisement", dto);
        return "marketing/update-campaign";
    }

    // Marketing Exec: Update post
    @PostMapping("/campaigns/{id}")
    public String update(@PathVariable Long id, @ModelAttribute AdvertisementDTO dto) {
        service.update(id, dto);
        return "redirect:/marketing/campaigns";
    }

    // Marketing Exec: Delete
    @GetMapping("/campaigns/delete/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/marketing/campaigns";
    }

    // Customer: View ongoing promotions
    @GetMapping("/promotions")
    public String viewPromotions(Model model) {
        List<AdvertisementDTO> promotions = service.getOngoing();
        // Increment views for each (simplified, in real: per user/session)
        promotions.forEach(p -> service.incrementView(p.getId()));
        model.addAttribute("promotions", promotions);
        return "marketing/view-promotions";
    }

    // Simulate click tracking (e.g., called via JS on click)
    @PostMapping("/promotions/click/{id}")
    @ResponseBody
    public void trackClick(@PathVariable Long id) {
        service.incrementClick(id);
    }
}