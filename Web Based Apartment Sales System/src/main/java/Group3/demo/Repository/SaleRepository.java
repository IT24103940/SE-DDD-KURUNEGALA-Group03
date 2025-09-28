package Group3.demo.Repository;

import Group3.demo.Entity.Sale;
import Group3.demo.Entity.enums.SaleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByLead_LeadId(Long leadId);
    long countByStatus(SaleStatus status);
    List<Sale> findByStatusAndContractDateBetween(SaleStatus status, LocalDate start, LocalDate end);
}
