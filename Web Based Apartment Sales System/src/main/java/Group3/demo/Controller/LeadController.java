package Group3.demo.Controller;

import Group3.demo.Entity.Lead;
import Group3.demo.Entity.enums.LeadStatus;
import Group3.demo.Service.LeadService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadController {
    private final LeadService service;
    public LeadController(LeadService service) { this.service = service; }

    @PostMapping
    public Lead create(@Valid @RequestBody Lead lead) { return service.createLead(lead); }

    @GetMapping
    public List<Lead> all() { return service.getAll(); }

    @GetMapping("/{id}")
    public Lead get(@PathVariable Long id) { return service.get(id); }

    @PutMapping("/{id}")
    public Lead update(@PathVariable Long id, @RequestBody Lead update) { return service.update(id, update); }

    @PutMapping("/{id}/assign")
    public Lead assign(@PathVariable Long id, @RequestParam String to) { return service.assign(id, to); }

    @GetMapping("/status/{status}")
    public List<Lead> byStatus(@PathVariable LeadStatus status) { return service.byStatus(status); }

    @GetMapping("/assignee/{user}")
    public List<Lead> byAssignee(@PathVariable String user) { return service.byAssignee(user); }
}
