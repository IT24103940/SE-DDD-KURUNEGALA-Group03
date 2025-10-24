package Group3.demo.Controller;

import Group3.demo.DTO.LeadDTO;
import Group3.demo.Entity.enums.LeadStatus;
import Group3.demo.Repository.ApartmentRepository;
import Group3.demo.Repository.UserRepository;
import Group3.demo.Service.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesManagerController {

    private final SalesService salesService;
    private final ApartmentRepository apartmentRepo;
    private final UserRepository userRepo;

    @GetMapping("/leads")
    public String leads(Model model) {
        model.addAttribute("leads", salesService.listLeads());
        return "sales/leads";
    }

    @GetMapping("/leads/new")
    public String newLead(Model model) {
        var dto = new LeadDTO();
        model.addAttribute("lead", dto);
        model.addAttribute("apartments", apartmentRepo.findAll());
        model.addAttribute("customers", userRepo.findAll().stream()
                .filter(u -> u.getType().name().equals("CUSTOMER")).toList());
        model.addAttribute("statuses", LeadStatus.values());
        return "sales/lead-form";
    }

    @PostMapping("/leads")
    public String createLead(@ModelAttribute("lead") @Valid LeadDTO dto, Authentication authentication) {
        // auto-assign to the logged-in Sales Manager
        var current = userRepo.findByUsername(authentication.getName()).orElseThrow();
        dto.setAssignedToId(current.getId());
        salesService.createLead(dto);
        return "redirect:/sales/leads";
    }

    // Show edit form
    @GetMapping("/leads/{id}/edit")
    public String editLeadForm(@PathVariable("id") Integer id, Model model) {
        LeadDTO lead = salesService.getLeadById(id);
        model.addAttribute("lead", lead);
        model.addAttribute("apartments", apartmentRepo.findAll());
        model.addAttribute("customers", userRepo.findAll().stream()
                .filter(u -> u.getType().name().equals("CUSTOMER")).toList());
        model.addAttribute("statuses", LeadStatus.values());
        return "sales/lead-form";
    }

    // Update lead
    @PostMapping("/leads/{id}/update")
    public String updateLead(@PathVariable("id") Integer id, @ModelAttribute("lead") LeadDTO lead, Authentication authentication) {
        // keep assigned to current sales manager
        var current = userRepo.findByUsername(authentication.getName()).orElseThrow();
        lead.setAssignedToId(current.getId());
        salesService.updateLead(id, lead);
        return "redirect:/sales/leads";
    }

    // Delete lead
    @GetMapping("/leads/{id}/delete")
    public String deleteLead(@PathVariable("id") Integer id, Model model) {
        try {
            salesService.deleteLead(id);
            return "redirect:/sales/leads";
        } catch (RuntimeException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("leads", salesService.listLeads());
            return "sales/leads"; // return to leads page with error
        }
    }

}
