package Group3.demo.Repository;

import Group3.demo.Entity.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {
    List<Interaction> findByLead_LeadId(Long leadId);
}
