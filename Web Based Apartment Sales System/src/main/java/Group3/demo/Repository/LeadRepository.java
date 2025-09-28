package Group3.demo.Repository;

import Group3.demo.Entity.Lead;
import Group3.demo.Entity.enums.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByStatus(LeadStatus status);
    List<Lead> findByAssignedTo(String assignedTo);
}
