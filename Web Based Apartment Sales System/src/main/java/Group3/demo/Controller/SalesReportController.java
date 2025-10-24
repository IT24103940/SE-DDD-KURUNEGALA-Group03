package Group3.demo.Controller;

import Group3.demo.Entity.Lead;
import Group3.demo.Entity.enums.LeadStatus;
import Group3.demo.Repository.LeadRepository;
import Group3.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesReportController {

    private final LeadRepository leadRepo;
    private final UserRepository userRepo;

    @GetMapping("/report")
    public String report(Authentication auth, Model model) {
        var current = userRepo.findByUsername(auth.getName()).orElseThrow();
        List<Lead> myLeads = leadRepo.findByAssignedTo_Id(current.getId());

        Map<LeadStatus, Long> counts = new EnumMap<>(LeadStatus.class);
        for (LeadStatus ls : LeadStatus.values()) counts.put(ls, 0L);
        myLeads.forEach(l -> counts.compute(l.getStatus(), (k, v) -> (v == null ? 0 : v) + 1));

        model.addAttribute("counts", counts);
        model.addAttribute("leads", myLeads);
        return "sales/report";
    }
}

