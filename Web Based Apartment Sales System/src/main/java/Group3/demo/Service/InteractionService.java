package Group3.demo.Service;

import Group3.demo.Entity.Interaction;
import Group3.demo.Repository.InteractionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InteractionService {
    private final InteractionRepository repo;
    public InteractionService(InteractionRepository repo) { this.repo = repo; }
    public Interaction create(Interaction i) { return repo.save(i); }
    public List<Interaction> byLead(Long leadId) { return repo.findByLead_LeadId(leadId); }
}
