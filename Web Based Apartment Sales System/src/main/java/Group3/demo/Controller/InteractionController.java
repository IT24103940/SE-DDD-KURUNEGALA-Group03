package Group3.demo.Controller;

import Group3.demo.Entity.Interaction;
import Group3.demo.Service.InteractionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {
    private final InteractionService service;
    public InteractionController(InteractionService service) { this.service = service; }

    @PostMapping
    public Interaction create(@RequestBody Interaction i) { return service.create(i); }

    @GetMapping("/lead/{leadId}")
    public List<Interaction> byLead(@PathVariable Long leadId) { return service.byLead(leadId); }
}
