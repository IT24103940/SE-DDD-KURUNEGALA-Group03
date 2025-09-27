package Group3.demo.Controller;

import Group3.demo.Entity.Sale;
import Group3.demo.Service.SaleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {
    private final SaleService saleService;
    public ReportController(SaleService saleService) { this.saleService = saleService; }

    @PostMapping("/sales")
    public Sale create(@RequestBody Sale s) { return saleService.create(s); }

    @GetMapping("/sales")
    public List<Sale> all() { return saleService.all(); }

    @GetMapping("/sales/lead/{leadId}")
    public List<Sale> byLead(@PathVariable Long leadId) { return saleService.byLead(leadId); }

    @PutMapping("/sales/{id}")
    public Sale update(@PathVariable Long id, @RequestBody Sale upd) { return saleService.update(id, upd); }

    @GetMapping("/reports/summary")
    public Map<String, Object> summary(@RequestParam int year) {
        return saleService.summary(year);
    }
}
