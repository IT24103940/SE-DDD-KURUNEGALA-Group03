package Group3.demo.Service;

import Group3.demo.Entity.Customer;
import Group3.demo.Entity.Lead;
import Group3.demo.Entity.enums.LeadStatus;
import Group3.demo.Repository.LeadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeadService {
    private final LeadRepository repo;
    private final CustomerService customerService;

    public LeadService(LeadRepository repo, CustomerService customerService) {
        this.repo = repo;
        this.customerService = customerService;
    }

    @Transactional
    public Lead createLead(Lead lead) {
        // ensure customer exists (by email)
        Customer savedCustomer = customerService.createOrGet(lead.getCustomer());
        lead.setCustomer(savedCustomer);
        return repo.save(lead);
    }

    public List<Lead> getAll() {                 // <-- findAll() (not findALL)
        return repo.findAll();
    }

    public Lead get(Long id) {
        return repo.findById(id)                  // <-- orElseThrow (correct spelling)
                .orElseThrow(() -> new IllegalArgumentException("Lead not found: " + id));
    }

    public List<Lead> byStatus(LeadStatus status) { return repo.findByStatus(status); }
    public List<Lead> byAssignee(String assignee) { return repo.findByAssignedTo(assignee); }

    public Lead update(Long id, Lead update) {
        Lead l = get(id);
        if (update.getSource() != null)       l.setSource(update.getSource());
        if (update.getStatus() != null)       l.setStatus(update.getStatus());
        if (update.getAssignedTo() != null)   l.setAssignedTo(update.getAssignedTo());
        if (update.getApartmentCode() != null)l.setApartmentCode(update.getApartmentCode());
        if (update.getBudget() != null)       l.setBudget(update.getBudget());
        return repo.save(l);
    }

    public Lead assign(Long id, String toUser) {
        Lead l = get(id);
        l.setAssignedTo(toUser);
        return repo.save(l);
    }
}
