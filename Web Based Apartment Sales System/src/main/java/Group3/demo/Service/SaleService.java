package Group3.demo.Service;

import Group3.demo.Entity.Sale;
import Group3.demo.Entity.enums.SaleStatus;
import Group3.demo.Repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SaleService {
    private final SaleRepository repo;
    public SaleService(SaleRepository repo) { this.repo = repo; }

    public Sale create(Sale s) { return repo.save(s); }
    public List<Sale> all() { return repo.findAll(); }

    public Sale get(Long id) {
        return repo.findById(id)                      // <-- orElseThrow (correct)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found: " + id));
    }

    public List<Sale> byLead(Long leadId) { return repo.findByLead_LeadId(leadId); }

    public Sale update(Long id, Sale upd) {
        Sale s = get(id);
        if (upd.getPrice() != null)          s.setPrice(upd.getPrice());
        if (upd.getStatus() != null)         s.setStatus(upd.getStatus());
        if (upd.getPaymentStatus() != null)  s.setPaymentStatus(upd.getPaymentStatus());
        if (upd.getApartmentCode() != null)  s.setApartmentCode(upd.getApartmentCode());
        if (upd.getContractDate() != null)   s.setContractDate(upd.getContractDate());
        return repo.save(s);
    }

    public Map<String, Object> summary(int year) {
        Map<String, Object> m = new LinkedHashMap<>();

        long totalSales = repo.count();
        long completed  = repo.countByStatus(SaleStatus.COMPLETED);
        long pending    = repo.countByStatus(SaleStatus.PENDING);
        long cancelled  = repo.countByStatus(SaleStatus.CANCELLED);

        m.put("totalSales", totalSales);

        // Use a simple map to avoid any JDK version quirks with Map.of
        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("COMPLETED", completed);
        byStatus.put("PENDING",   pending);
        byStatus.put("CANCELLED", cancelled);
        m.put("byStatus", byStatus);

        // Monthly totals (completed only)
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end   = LocalDate.of(year, 12, 31);

        List<Sale> list = repo.findByStatusAndContractDateBetween(SaleStatus.COMPLETED, start, end);
        Map<Integer, Long> monthCounts = list.stream()
                .filter(s -> s.getContractDate() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getContractDate().getMonthValue(),  // <-- no stray 'T' before s
                        TreeMap::new,
                        Collectors.counting()
                ));

        m.put("monthlyCompleted", monthCounts);
        return m;
    }
}
