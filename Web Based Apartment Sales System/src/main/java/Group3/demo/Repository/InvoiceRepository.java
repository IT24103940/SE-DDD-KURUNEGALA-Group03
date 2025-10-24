package Group3.demo.Repository;

import Group3.demo.Entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findBySalesOrderId(Integer salesOrderId);
}
